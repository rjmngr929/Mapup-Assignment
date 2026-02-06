package com.my.mapupassessment.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import org.osmdroid.util.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


object Helper {

    @SuppressLint("DefaultLocale")
    fun formatTime(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        if(hours > 0){
            return String.format("%02d hour %02d minutes %02d second", hours, minutes, seconds)
        }else if(minutes > 0){
            return String.format("%02d minutes %02d seconds",  minutes, seconds)
        }else{
            return String.format("%02d seconds", seconds)
        }


    }

    fun isDarkMode(context: Context): Boolean {
        val mode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }



    fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters < 1000) {
            // Less than 1 km → show in meters
            "${distanceMeters.toInt()} meter"
        } else {
            // 1 km or more → show km + meters
            val km = distanceMeters.toInt() / 1000
            val meters = distanceMeters.toInt() % 1000
            if (meters == 0) {
                "${km} km"
            } else {
                "${km} km ${meters} meter"
            }
        }
    }


    fun calculateTotalDistance(polyLineData: List<GeoPoint>): Double {
        if (polyLineData.size < 2) return 0.0

        var totalDistance = 0.0

        for (i in 0 until polyLineData.size - 1) {
            totalDistance += haversine(
                polyLineData[i].latitude,
                polyLineData[i].longitude,
                polyLineData[i + 1].latitude,
                polyLineData[i + 1].longitude
            )
        }

        return totalDistance // meters
    }

    fun haversine(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth radius in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

}
