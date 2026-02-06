package com.my.mapupassessment.service


import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.my.mapupassessment.repository.SessionLocationRepository
import com.my.mapupassessment.session.SessionManager
import com.my.mapupassessment.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import com.my.mapupassessment.utils.MapUtils
import com.my.mapupassessment.utils.PermissionHelper.isLocationEnabled
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class ForegroundLocationService : Service() {

    companion object {
        private const val TAG = "ForegroundLocationService"
        private const val MIN_DISTANCE_METERS = 5f
        private const val MAX_ACCEPTABLE_ACCURACY = 30f // 🔥 important
        private const val STALE_LOCATION_TIME_MS = 10_000L

        const val ACTION_STOP = "ACTION_STOP_TRACKING"
    }


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionLocationRepository: SessionLocationRepository

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var serviceScope: CoroutineScope

    private var lastStoredLocation: Location? = null

    private var lastGoodLocation: Location? = null
    private var lastLocationTime: Long = 0L


    // 🔥 In-memory path (GeoPoints)
    private val geoPointPath = mutableListOf<GeoPoint>()

    private lateinit var locationManager: LocationManager
    private lateinit var gpsReceiver: BroadcastReceiver


    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()

        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)


//        ******************* Register GPS Check Receiver ********************
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        gpsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    if (!isLocationEnabled(this@ForegroundLocationService)) {
                        stopService()
                    }
                }
            }
        }

        registerReceiver(
            gpsReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
//        ******************* Register Location Check Receiver ********************

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        )
        .setMinUpdateIntervalMillis(2_000L) // fastest acceptable
        .setMaxUpdateDelayMillis(10_000L)   // batching for Doze
        .build()

        locationCallback = object : LocationCallback() {

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {

                    Log.d(TAG, "onLocationAvailability: enter that location availability section => ${availability.isLocationAvailable}")

                    if(!isLocationEnabled(this@ForegroundLocationService)){
                        stopService()
                    }
//                    stopService()
                }
            }

            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (ActivityCompat.checkSelfPermission(
                            this@ForegroundLocationService,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            this@ForegroundLocationService,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        stopService()
                        return
                    }

                    val accuracy = location.accuracy
                    val now = System.currentTimeMillis()

                    if (accuracy > MAX_ACCEPTABLE_ACCURACY) {
                        Log.w(TAG, "Poor GPS accuracy: $accuracy m")

                        // fallback if no update for long time
                        if (lastGoodLocation != null &&
                            now - lastLocationTime > STALE_LOCATION_TIME_MS
                        ) {
                            handleNewLocation(lastGoodLocation!!)
                        }
                        return
                    }

                    // 🟢 Good GPS
                    lastGoodLocation = location
                    lastLocationTime = now

                    handleNewLocation(location)

                }
            }
        }
    }

    private fun handleNewLocation(location: Location) {
        serviceScope.launch {
            val sessionId = sessionManager.getActiveSessionId()
                ?: return@launch

            if (lastStoredLocation == null) {
                storeLocation(location, sessionId, 0.0)
                return@launch
            }

            val distance = lastStoredLocation!!.distanceTo(location)

            if (distance < MIN_DISTANCE_METERS) {
                Log.d(TAG, "Ignoring small GPS jump: $distance m")
                return@launch
            }

//            if (distance > 100) {
//                Log.w(TAG, "Ignoring unrealistic jump: $distance m")
//                return@launch
//            }

            Log.d(TAG, "Moved $distance meters → storing point")
            storeLocation(location, sessionId, distance.toDouble())
        }
    }

    private suspend fun storeLocation(location: Location, sessionId: Long, distance: Double) {
        lastStoredLocation = location

        val geoPoint = GeoPoint(
            location.latitude,
            location.longitude
        )

        geoPointPath.add(geoPoint)

        val encodedPolyline = MapUtils.encodeGeoPoints(geoPointPath)

        sessionLocationRepository.saveEncodedPath(
            sessionId = sessionId,
            encodedPolyline = encodedPolyline
        )

        sessionLocationRepository.addDistance(
            sessionId,
            distance = distance
        )

        Log.d(
            TAG,
            "Stored point | Accuracy=${location.accuracy}m | Path size=${geoPointPath.size}"
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (!PermissionHelper.hasRequiredPermissions(this)) {
            stopService()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_STOP) {
            stopService()
            return START_NOT_STICKY
        }

        val notification = NotificationHelper.createNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(notificationId, notification)
        }

        startTracking()
        return START_STICKY
    }


    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopService()
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {


        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

//        val job = serviceScope.launch {
//            try {
//                sessionManager.stopSession()
//                sharedPrefManager.setTracking(false)
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to stop session", e)
//            }
//        }

//        job.invokeOnCompletion {
//            serviceScope.cancel()
//        }

        try {
            unregisterReceiver(gpsReceiver)
        } catch (e: IllegalArgumentException) {
            // already unregistered
        }

        serviceScope.cancel() // cancel remaining coroutines ONCE

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        Log.d(TAG, "Service destroyed")

        super.onDestroy()


    }

    override fun onBind(intent: Intent?) = null

    fun stopService(){
        serviceScope.launch {
            try {
                sessionManager.stopSession()   // suspend safe
            } catch (e: CancellationException) {
                // normal, ignore
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop session", e)
            } finally {
                stopSelf()   // 👈 onDestroy trigger hoga
            }
        }

    }

}
