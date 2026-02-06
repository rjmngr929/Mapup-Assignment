package com.my.mapupassessment.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.my.mapupassessment.session.SessionManager
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import javax.inject.Inject


class StopTrackingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
//        context?.let {
//            val stopIntent = Intent(it, ForegroundLocationService::class.java)
//            stopIntent.action = ForegroundLocationService.ACTION_STOP
//            it.stopService(stopIntent)
//        }

        context?.let {
            val stopIntent = Intent(it, ForegroundLocationService::class.java).apply {
                action = ForegroundLocationService.ACTION_STOP
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.startForegroundService(stopIntent)
            } else {
                it.startService(stopIntent)
            }
        }
    }
}
