package com.my.mapupassessment.utils

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.my.mapupassessment.Helper.MarkerLabelOverlay
import com.my.mapupassessment.R
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

object MapUtils {

    fun encodeGeoPoints(points: List<GeoPoint>): String {
        var lastLat = 0
        var lastLng = 0
        val result = StringBuilder()

        for (point in points) {
            val lat = (point.latitude * 1e5).toInt()
            val lng = (point.longitude * 1e5).toInt()

            result.append(encodeValue(lat - lastLat))
            result.append(encodeValue(lng - lastLng))

            lastLat = lat
            lastLng = lng
        }
        return result.toString()
    }


    private fun encodeValue(value: Int): String {
        var v = value shl 1
        if (value < 0) v = v.inv()

        val result = StringBuilder()
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f)) + 63).toChar())
            v = v shr 5
        }
        result.append((v + 63).toChar())
        return result.toString()
    }


    fun decodePolylineToGeoPoints(encoded: String): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length

        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dLat = if ((result and 1) != 0)
                (result shr 1).inv()
            else
                (result shr 1)

            lat += dLat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dLng = if ((result and 1) != 0)
                (result shr 1).inv()
            else
                (result shr 1)

            lng += dLng

            points.add(
                GeoPoint(
                    lat / 1E5,
                    lng / 1E5
                )
            )
        }

        return points
    }

    fun updatePolyline(mapView: MapView, polylinePoints: List<GeoPoint>) {

        val mapView = mapView

        mapView.removeAllViews()

        // Draw polyline
        val polyline = Polyline()
        polyline.setPoints(polylinePoints)
        polyline.outlinePaint.color = Color.BLACK
        polyline.outlinePaint.strokeWidth = 8f
        mapView.overlays.add(polyline)

        addSourceAndDestinationMarkers(mapView, polylinePoints)
        animateCameraToRoute(mapView, polylinePoints)

        mapView.invalidate()

    }


    fun addSourceAndDestinationMarkers(
        mapView: MapView,
        geoPoints: List<GeoPoint>
    ) {

        if (geoPoints.size < 2) return

        clearSessionMarkers(mapView)

        val startPoint = geoPoints.first()
        val endPoint = geoPoints.last()

        // 🔵 Source Marker
        val startMarker = Marker(mapView)
        startMarker.position = startPoint
        startMarker.title = "Start Location"
        startMarker.subDescription = "Tracking started here"
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.setInfoWindow(null)
        startMarker.relatedObject = "SESSION_MARKER"
        startMarker.icon =
            ContextCompat.getDrawable(mapView.context, R.drawable.location)

        // 🔴 Destination Marker
        val endMarker = Marker(mapView)
        endMarker.position = endPoint
        endMarker.title = "End Location"
        endMarker.subDescription = "Tracking ended here"
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        endMarker.setInfoWindow(null)
        endMarker.relatedObject = "SESSION_MARKER"
        endMarker.icon =
            ContextCompat.getDrawable(mapView.context, R.drawable.location)





        mapView.overlays.add(startMarker)
        mapView.overlays.add(endMarker)

        mapView.overlays.add(
            MarkerLabelOverlay(startPoint, "Start Location").apply {
                tag = "SESSION_LABEL"
            }
        )

        mapView.overlays.add(
            MarkerLabelOverlay(endPoint, "End Location").apply {
                tag = "SESSION_LABEL"
            }
        )

        // 👇 Auto show snippet
//        startMarker.showInfoWindow()
//        endMarker.showInfoWindow()
    }

    fun animateCameraToRoute(
        mapView: MapView,
        geoPoints: List<GeoPoint>
    ) {
        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)

        mapView.zoomToBoundingBox(
            boundingBox,
            true,   // animate
            100     // padding (px)
        )
    }

    fun clearSessionMarkers(mapView: MapView) {
        mapView.overlays.removeAll { overlay ->
            (overlay is Marker && overlay.relatedObject == "SESSION_MARKER") ||
                    (overlay is MarkerLabelOverlay && overlay.tag == "SESSION_LABEL")
        }
    }

}