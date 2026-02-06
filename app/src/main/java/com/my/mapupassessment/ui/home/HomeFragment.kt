package com.my.mapupassessment.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.mapupassessment.R
import com.my.mapupassessment.databinding.FragmentHomeBinding
import com.my.mapupassessment.ui.history.SessionHistoryViewModel
import com.my.mapupassessment.utils.Helper.formatDistance
import com.my.mapupassessment.utils.Helper.formatTime
import com.my.mapupassessment.utils.PermissionHelper
import com.my.mapupassessment.utils.customAlertDialogAnim
import com.my.mapupassessment.utils.gone
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import com.my.mapupassessment.utils.showPermanentlyDeniedDialog
import com.my.mapupassessment.utils.showRationaleDialog
import com.my.mapupassessment.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import javax.inject.Inject
import kotlin.getValue
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.my.mapupassessment.utils.Constants
import com.my.mapupassessment.utils.Helper.isDarkMode
import com.my.mapupassessment.utils.PermissionHelper.permissionLabel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource


@AndroidEntryPoint
class HomeFragment : Fragment(), PermissionRequest.Listener {

    companion object {
        private const val TAG = "Home Fragment"
    }

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val sessionHistoryViewModel: SessionHistoryViewModel by activityViewModels()

    private lateinit var myContext: Context

    private val request by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION ).build()
        }else{
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                ).build()
        }
    }

    private val GPSLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(PermissionHelper.isLocationEnabled(myContext)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        Configuration.getInstance().load(myContext.applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(myContext.applicationContext))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(myContext)

        request.addListener(this)

        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("BatteryLife")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(PermissionHelper.isLocationEnabled(myContext)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }

//        homeViewModel.clearDataBase()

        binding.themeToggleBtn.isChecked = isDarkMode(myContext)

        binding.themeToggleBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            // Responds to switch being checked/unchecked
            toggleAppTheme()
        }

        binding.btnStartStop.setOnClickListener {
            if(PermissionHelper.isLocationEnabled(myContext)){
                if (!PermissionHelper.hasRequiredPermissions(myContext)) {
                    return@setOnClickListener request.send()
                }
                homeViewModel.toggleTracking()
            }else{
                promptUserToEnableLocation()
            }
        }


        lifecycleScope.launch {
            homeViewModel.isTracking.collect {tracking->
                Log.d(TAG, "onViewCreated: tracking status => $tracking")
                if(tracking){
                    binding.tvStatus.text = "Live tracking active"
                    binding.btnStartStop.text = "Stop"
                    binding.btnSessionHistory.gone()
                    binding.tvTimer.visible()
                }else{
                    binding.tvStatus.text = "Start Tracking"
                    binding.btnStartStop.text = "Start"
                    homeViewModel.stopTimer()
                    binding.btnSessionHistory.visible()
                    binding.tvTimer.gone()
                }
            }
        }

        homeViewModel.elapsedTime.observe(viewLifecycleOwner, Observer{timer ->
            binding.tvTimer.text = String.format("Duration: %s", formatTime (timer))
        })

        binding.btnSessionHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        sessionHistoryViewModel.allSessions.observe(viewLifecycleOwner) {sessionList ->
           
            binding.totalSessionText.text = sessionList.size.toString()

            val totalDistance = sessionList.sumOf { it.distance }
            
            binding.totalDistanceText.text = formatDistance(totalDistance)
            
        }



    }

    override fun onStart() {
        super.onStart()
        homeViewModel.resumeTimerIfNeeded()
    }

    override fun onStop() {
        super.onStop()
        // UI invisible → stop UI timer
        homeViewModel.stopTimer()
    }

    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyPermanentlyDenied() -> {
                val permanentlyDeniedPermissions = result
                    .filterIsInstance<PermissionStatus.Denied.Permanently>()
                    .map { it.permission }

                val deniedPermissionNames = permanentlyDeniedPermissions
                    .map { permissionLabel(it) }   // map first
                    .distinct()                    // then dedupe
                    .joinToString(separator = "\n• ", prefix = "• ")


                val message = buildString {
                    append("The following permissions are disabled and required for proper app functionality:\n\n")
                    append(deniedPermissionNames)
                    append("\n\nPlease enable them from App Settings.")
                }

                myContext.showPermanentlyDeniedDialog(result, message){
                    val intent = Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", myContext.packageName, null)
                    }
                    GPSLauncher.launch(intent)
                }


            }
            result.anyShouldShowRationale() ->{
                val deniedPermissions = result
                    .filterIsInstance<PermissionStatus.Denied>()
                    .map { it.permission }


                val deniedPermissionNames = deniedPermissions
                    .map { permissionLabel(it) }   // map first
                    .distinct()                    // then dedupe
                    .joinToString(separator = "\n• ", prefix = "• ")

                val message = buildString {
                    append("The following permissions are disabled and required for proper app functionality:\n\n")
                    append(deniedPermissionNames)
                    append("\n\nPlease enable them from App Settings.")
                }

                myContext.showRationaleDialog(result, request, message)

            }
            result.allGranted() -> {
                val pm = myContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(myContext.packageName)) {
                    // show explanation dialog

                    AlertDialog.Builder(myContext)
                        .setTitle("Background Permission Required")
                        .setMessage(
                            "For uninterrupted GPS tracking, please allow background location.\n\n" +
                                    "Some devices restrict apps to save battery, which may stop location updates when the app is not in use."
                        )
                        .setPositiveButton("Allow") { _, _ ->
                            val intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = "package:${myContext.packageName}".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("Not Now", null)
                        .show()
                }
            }
        }
    }


    private fun promptUserToEnableLocation() {
        myContext.customAlertDialogAnim(myContext,"Location Services Disabled", "Location services are required for this app to function correctly. Please enable them.", btnText = "Ok"){
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            GPSLauncher.launch(intent)
        }
    }

    fun toggleAppTheme() {
        val current =
            AppCompatDelegate.getDefaultNightMode()

        val newMode =
            if (current == AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.MODE_NIGHT_NO
            else
                AppCompatDelegate.MODE_NIGHT_YES

        sharedPrefManager.putInt(Constants.THEME_TOGGLE, newMode)
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun getCurrentLocation() {
//        fusedLocationClient.getCurrentLocation(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            null
//        ).addOnSuccessListener { location ->
//            if (location != null) {
//                val latitude = location.latitude
//                val longitude = location.longitude
//
//                Log.d("LOCATION", "Lat: $latitude, Lng: $longitude")
//
//                sharedPrefManager.saveCurrentLatLng(Constants.CURRENT_LATLNG, GeoPoint(latitude, longitude))
//
//                val mapView = binding.mapView
//                mapView.controller.setZoom(15.0)
//                mapView.controller.animateTo(GeoPoint(latitude, longitude)) // Jodhpur
//                mapView.setTileSource(TileSourceFactory.MAPNIK)
//                mapView.setMultiTouchControls(true)
//                mapView.isTilesScaledToDpi = true
//                mapView.setUseDataConnection(true)
//
//                mapView.overlayManager.tilesOverlay.setColorFilter(
//                    PorterDuffColorFilter(
//                        Color.LTGRAY,
//                        PorterDuff.Mode.MULTIPLY
//                    )
//                )
//
//
//                val locationOverlay = MyLocationNewOverlay(
//                    GpsMyLocationProvider(myContext),
//                    mapView
//                )
//
//                locationOverlay.runOnFirstFix {
//                    requireActivity().runOnUiThread {
//                        mapView.controller.animateTo(locationOverlay.myLocation)
//                        mapView.controller.setZoom(18.0)
//                    }
//                }
//
//                locationOverlay.enableMyLocation()      // location dot
//                locationOverlay.enableFollowLocation()  // camera follow
//                locationOverlay.isDrawAccuracyEnabled = true
//
//                mapView.overlays.add(locationOverlay)
//
////                val marker = org.osmdroid.views.overlay.Marker(mapView)
////                marker.position = GeoPoint(latitude, longitude)
////                marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.location)
////                mapView.overlays.add(marker)
//
//            } else {
//                Log.e("LOCATION", "Location is null")
//            }
//        }
//    }

}