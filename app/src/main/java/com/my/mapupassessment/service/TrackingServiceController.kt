package com.my.mapupassessment.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

object TrackingServiceController {

    fun start(context: Context) {
        Log.d("TAG", "start: service start success")
        val intent = Intent(context, ForegroundLocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, ForegroundLocationService::class.java)
        context.stopService(intent)
    }
}
