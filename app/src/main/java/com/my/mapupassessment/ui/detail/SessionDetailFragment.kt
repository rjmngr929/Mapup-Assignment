package com.my.mapupassessment.ui.detail

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.mapupassessment.R
import com.my.mapupassessment.databinding.FragmentSessionDetailBinding
import com.my.mapupassessment.repository.SessionTollRepository
import com.my.mapupassessment.ui.home.HomeFragment
import com.my.mapupassessment.utils.Constants
import com.my.mapupassessment.utils.Helper.formatDistance
import com.my.mapupassessment.utils.Helper.formatTime
import com.my.mapupassessment.utils.Helper.isDarkMode
import com.my.mapupassessment.utils.MapUtils.addSourceAndDestinationMarkers
import com.my.mapupassessment.utils.MapUtils.updatePolyline
import com.my.mapupassessment.utils.NetworkResult
import com.my.mapupassessment.utils.PermissionHelper
import com.my.mapupassessment.utils.customAlertDialogAnim
import com.my.mapupassessment.utils.getLoadingDialog
import com.my.mapupassessment.utils.gone
import com.my.mapupassessment.utils.hideLoader
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import com.my.mapupassessment.utils.showLoader
import com.my.mapupassessment.utils.showPermanentlyDeniedDialog
import com.my.mapupassessment.utils.showRationaleDialog
import com.my.mapupassessment.utils.showSnack
import com.my.mapupassessment.utils.showToast
import com.my.mapupassessment.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class SessionDetailFragment : Fragment(), PermissionRequest.Listener {

    companion object {
        private const val TAG = "Session Detail Fragment"
    }

    private lateinit var binding: FragmentSessionDetailBinding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val viewModel: SessionDetailViewModel by activityViewModels()

    private lateinit var myContext: Context

    private lateinit var loader: androidx.appcompat.app.AlertDialog

    private val polyLineFetch = "_sl_Da_t|L@BXg@TIh@_@\\\\Wr@_@n@a@JOpBiAdBcAhAq@tBsACEAG?E@GBCBCDCD?D?DBlA]z@Yh@OVGRA@?T@VHfBbA|@`@nAd@`A^|Bx@JD|Bl@fCx@hAVl@RNFfA`@RHTJd@Xf@Z|@n@p@b@z@j@ZXr@l@TNRHPFNBVBhAF~AF~AB^?`@@Z@^B`@HTINGJELELILK@SN{EFw@?GFm@TaAFU\\\\y@Xu@BE^{@N]t@?L?l@DlAHzAJtBPrDV`AHd@D^Hb@Fd@Fn@FdF^dDTN@jHf@dCRxDVbCPpCRhG`@~@HF?`Ih@zLt@`Lz@T@tCT|Hl@rF`@hPbA`OjAfOhAfCFb@An@EDEDAH?XCVE`AY|@_@`@Wh@e@n@s@h@w@Rc@JYJUJa@XcARiApCmNxBoKPaAbBkIp@iDhBgJTgAjBgJNs@Nq@hBgJh@iCbBcJPcAtAuHVyAPiAp@aEReARgA`@uBRcAPw@Lu@X_BD]D]Am@?G@KBIBCFEHAH?F@DDBD@BVNRFxE~@lBVzANv@DfA@z@?v@?t@Ch@CrAMh@Eh@Ih@K~@SdAWh@OjA[f@KlBe@j@Ml@Ml@K|@MbAMr@KHAl@ItCa@xBWbAId@CdJi@bBI|@EpDSpUgAfIa@lCMpKk@`H[dG[tKi@lDQjFM^ARAxHMlFIL?~EIF?jBCTAfAExAApB@bADv@D`DPh@Bv@?`@Ah@CZEXGvEiAnA[rBk@rEwA~C_A|@YdFaBpHcCbCi@REXGvAUtAKLCbAGpCAnE?hOEjC?tHIpACjBGrM[rN]tCKhFShH_@|BM`COdG[xKo@nKg@lJa@lESfNm@lGYnDKbMm@bDSn@G`AMbB]nBk@xBq@dA_@hBo@nC_AdJyCPGNG~OsFjDoAHCbDeAtHiCvC_ArJaDjGuBTIzDuAfB[hCSVCxAMtAKtFa@fE[dBMhCE`GAdACnACbFIhHOhBGrEIf@Al@C`AE`AGjCYjASjB[pB_@`Dk@hCe@bM{BtJeBnHsA`Cc@zQgDxCk@~Ck@jKiBjKiBlW{EnCc@xCe@dEm@~KkApEi@hLmAxQsBjGs@lCY|Ei@tKmAzIaAlMwApI_AjS{BhGq@vDc@zDg@d@IXE|Ey@pEq@lAQzBc@TEzLuBjHuArE}@`LqBvPuCpCi@pPmCvMyBxSeDhK}ApB[dEo@xAWxAYtDq@bDs@bDs@nJ{BxKgC\\\\IjJ{Bd@Mf@O~Ak@|@a@r@]bCyA`EiCx@m@~OcKvE{CbBeA~JqG`LkHpOeKzKmGfAs@fAq@fJcG~OaK`GuDh@a@xFuDjKcH~DgChRyLhDeCtAkAfA_AlDkDxRmSbBeBtG}GnEuE|OgPbGkGzCaDzCaDjHoHzG{G|AwAd@i@hAmAj@m@jG{G~GiHbHkHtDyDfDmDzF_GzEeFfHkHxD_ErG{GjFoFt@y@|B}BtByB~BgCtCwCfC_CbBcB`@c@lBoBn@q@r@u@lCsCjAiAv@q@~BgBx@m@t@c@XM|As@|Ak@vC{@PEvBo@zC{@rBo@fBc@hAS`BSbDWdBOdAGd@Et@El@GlCSnCQb@EfGc@dCYr@GDA^MtA_@~DwAtC}AtC}BtC_D~AaCf@w@nAsCh@uA|EwL|AgDrFoI^i@l@y@lCoD~CaE~CoEpDyFvD{GxAiCpHwMXi@^o@vGsLpBmDx@wAp@yAJSbFqKlDmHxF}Lh@iApEoJz@}At@kA`AsAfAkAjBeBrA_AdAq@lFeCdFkCxFqCBA|Aw@pCyAzAs@dEwBpD{Al@SpDoArCw@|Cs@tDaAxAi@dBy@dAs@fAy@~A{AxA}AnFiGv@u@f@c@t@k@bCaBhD}AbBk@dHgBbCg@`H{A~D{@nNyCdH}Ax@WtAg@p@]n@e@r@i@h@k@vCwCpCwC|AkBh@k@z@{@pAmA|@q@vC{BrB_BdDiCdB{Ax@y@r@u@vBeClB{B\\\\a@vGeIt@gAbBcCx@qA`HqLxHcL~B_DV_@`@i@fEsEjD}CdHcFnA{@nDcChF}DnAeAdJyJlBuBjA}Ah@{@tE}InH_OtCyF\\\\m@dA}AZ_@h@i@h@_@x@g@`@Up@WdA]j@Kn@KnDY|QwA|@IxBSrGe@tL_AjOiAnAI`Jq@|D[`AGLAx@IhFa@|Gk@~L{@dFa@jGc@bDWbDYrDg@zAWfASrBe@bCm@pBi@fCu@bEcA`EeAbBc@`D{@lCq@xDcA`Be@rBg@tEmAtFyApDaAzBg@r@OpBc@xBa@j@I`BUdBUvAStAQnAO`C_@dC]tCc@pAOrASfEs@nC_@tASdHy@`C]|ASz@Kl@Iz@QpAWl@Qt@W`@Uf@Yl@a@XUf@e@f@g@l@u@^m@\\\\s@Rg@b@yAf@sBhAmErC}Lz@uD~@iEfAmEl@sB`AiBdAuA^a@l@c@jAi@zEgBhC}@pIqCfFiC`CmA|@i@jD}BtCoBd@[rCiBhH{ExI{F|GiEnDcC|@k@h@[x@a@l@Wh@SbAYtKyBhGqAr@OlAUxCu@bGkBbHiC`GcCnB{@zAw@tDyB`@YrD{Bx@e@tAs@tAm@`DwA|@_@zEsBlD}AjOkG~@_@fGiCpAe@p@]jAm@zA}@bCcBrBcBx@u@`AcAt@w@nB}BjA}Ap@y@vBsCvBuC\\\\a@l@oAtDmExDiEtBoBEy@WkDEu@Iw@[wDQiBgAiJQiBEkCDmBFwCHeCHyCAgBQmC]}E[wEg@iKBkA@m@PcCB_@Ds@?YCm@IUQ_@cCqEI]Ie@?s@_@eJIu@IcDAW[}IKiCg@wMK}C]oGc@aIYgFE}@GiBEiBA{AAgD?uAAu@@eB?yAAkB?]AiAGgAGy@Iq@SkACQ]oB[iBm@mDKo@u@mDa@_Ba@wAm@qBc@iBEUG]QeAIo@CQC_@Co@?i@@WHg@DSJYHIDMRS\\\\UlAy@tA}@XQRSxA{@jCcBPGZ_@RSPYPg@Jo@NwAFm@VwBF_@HYL]LWTSh@Q`@MGCCE?IBEDCF?^KLG~@[p@SpA_@JCNILIT]?G@GBEDERk@bBmERm@HUCEAC"


    private var lastDrawnSize = 0

    private var routePolyline: Polyline? = null

    private lateinit var polyLine: String

    private val request by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION ).build()
        }else{
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).build()
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
        binding = FragmentSessionDetailBinding.inflate(inflater, container, false)

        request.addListener(this)

        loader = getLoadingDialog(myContext)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(PermissionHelper.isLocationEnabled(myContext)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }

        val sessionId = arguments?.getLong("sessionId") ?: return
        Log.d(TAG, "onViewCreated: session id for that => $sessionId")
//        viewModel.setSessionId(sessionId)
        viewModel.getSessionById(sessionId)

//        viewModel.sessionInfo.observe(viewLifecycleOwner) { session ->
//            Log.d(TAG, "onViewCreated: session detail => ${session?.id}")
//            if(session != null){
//                binding.tvDistance.text = String.format("Distance: %s", formatDistance(session.distance))
//                binding.tvDuration.text = String.format("Duration: %s", formatTime(session.duration))
//
//               viewModel.getPolyLineData(session.id)
//            }
//        }

        viewModel.sessionData.observe(viewLifecycleOwner) { session ->
            Log.d(TAG, "onViewCreated: session detail => ${session?.id}")
            if(session != null){
                binding.tvDistance.text = String.format("Distance: %s", formatDistance(session.distance))
                binding.tvDuration.text = String.format("Duration: %s", formatTime(session.duration))

                viewModel.getPolyLineData(session.id)
            }
        }

        viewModel.latlngData.observe(viewLifecycleOwner) { latlngAry ->
            if (latlngAry.isEmpty()) {
                warningAlert()
                return@observe
            }

            val safePoints =
                if (latlngAry.size > 600) latlngAry.takeLast(600) else latlngAry

            binding.mapView.overlays.removeAll { it is Polyline }

            lastDrawnSize = safePoints.size

            animatePolyline(binding.mapView, safePoints)
            addSourceAndDestinationMarkers(binding.mapView, safePoints)
            animateCameraToRoute(binding.mapView, safePoints)

//            if (routePolyline == null) {
//                routePolyline = Polyline().apply {
//                    outlinePaint.color = Color.BLACK
//                    outlinePaint.strokeWidth = 8f
//                }
//                binding.mapView.overlays.add(routePolyline)
//            }
//
//            if (safePoints.size != lastDrawnSize) {
//                routePolyline?.setPoints(safePoints)
//                binding.mapView.postInvalidate()
//                lastDrawnSize = safePoints.size
//            }


        }

        viewModel.polyLineData.observe(viewLifecycleOwner) { polyLineData ->
            polyLine = polyLineData
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFetchToll.setOnClickListener {
            if(lastDrawnSize > 10){
                if(::polyLine.isInitialized){
                    viewModel.fetchTollData(
                        mapProvider = "osm",
                        polyLine = polyLine,
                        vehicleType = "2AxlesAuto",
                        currency = "INR"
                    )
                }else{
                    view.showSnack("No Route found.")
                }
            }else{
                view.showSnack("No Route found for ftech toll data")
            }

        }

        fetchTollListener()

    }

    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyPermanentlyDenied() -> myContext.showPermanentlyDeniedDialog(result, "Please allowed Permission"){
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", myContext.packageName, null)
                }
                GPSLauncher.launch(intent)
            }
            result.anyShouldShowRationale() -> myContext.showRationaleDialog(result, request, "Please allowed Permission")
            result.allGranted() -> {
               mapInitialise()
            }
        }
    }

    private fun mapInitialise(){

        val currentLatLng = sharedPrefManager.getCurrentLatLng(Constants.CURRENT_LATLNG)

        val mapView = binding.mapView
        mapView.controller.setZoom(15.0)
        mapView.controller.animateTo(currentLatLng) // Jodhpur
//        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.isTilesScaledToDpi = true
        mapView.setUseDataConnection(true)

        if (isDarkMode(myContext)) {
            // Dark map
            val darkTileSource = XYTileSource(
                "CartoDark",
                0, 19, 256, ".png",
                arrayOf("https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/")
            )
            mapView.setTileSource(darkTileSource)
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
        }


    }

    private fun promptUserToEnableLocation() {
        myContext.customAlertDialogAnim(myContext,"Location Services Disabled", "Location services are required for this app to function correctly. Please enable them.", btnText = "Ok"){
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            GPSLauncher.launch(intent)
        }
    }

    private fun warningAlert(){
        var dialogBuilder = AlertDialog.Builder(myContext)
        val layoutView: View = LayoutInflater.from(myContext).inflate(R.layout.alertdialog_custom_layout, null)

        val alertTitle : TextView = layoutView.findViewById(R.id.alertdialog_title)
        val alertMessage : TextView = layoutView.findViewById(R.id.alertdialog_desc)
        val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_btn)


        dialogBuilder.setView(layoutView)
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()

        alertTitle.text = "Oops!"
        alertMessage.text = "Location Track not available."

        confirmBtn.setOnClickListener {
            findNavController().popBackStack()
            alertDialog.dismiss()
        }

    }

    private fun fetchTollListener() {
        viewModel.tollPriceResponseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    val res = it.data?.routeResModel

                    Log.d(TAG, "fetchTollListener: toll response data is ${res?.hasTolls} and ${res?.costsDetail}")

                    binding.btnFetchToll.gone()

                    if(res?.hasTolls == true){
                        binding.tvTollTag.visible()
                        binding.tvTollCash.visible()
                        binding.tvTollTag.text = String.format("Toll via FastTag : ₹ %s /-", res.costsDetail.tagCost)
                        binding.tvTollCash.text = String.format("Toll via Cash : ₹ %s /-", res.costsDetail.cashCost)
                    }else{
                        view?.showSnack("No Toll found on that route.")
                    }

                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "fetchTollListener: error data => $it")
                    hideLoader(myContext, loader)

                    AlertDialog.Builder(myContext)
                        .setTitle("Toll Info Error")
                        .setMessage(it.message.toString())
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()

                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(myContext, loader)
                }
            }
        })
    }

    private fun animateCameraToRoute(
        mapView: MapView,
        points: List<GeoPoint>
    ) {
        if (points.isEmpty()) return

        val bbox = BoundingBox.fromGeoPoints(points)

        mapView.post {
            mapView.zoomToBoundingBox(bbox, true, 250)
        }
    }

    private fun animatePolyline(
        mapView: MapView,
        allPoints: List<GeoPoint>
    ) {
        if (allPoints.isEmpty()) return

        val polyline = Polyline().apply {
            outlinePaint.color = Color.BLACK
            outlinePaint.strokeWidth = 8f
        }

        mapView.overlays.add(polyline)

        val animatedPoints = mutableListOf<GeoPoint>()
        val handler = Handler(Looper.getMainLooper())

        val step = 8  // kitne points per frame (tuneable)
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                if (index >= allPoints.size) return

                val nextIndex = min(index + step, allPoints.size)
                animatedPoints.addAll(allPoints.subList(index, nextIndex))
                polyline.setPoints(animatedPoints)

                mapView.invalidate()

                index = nextIndex
                handler.postDelayed(this, 16) // ~60fps
            }
        }

        handler.post(runnable)
    }

}