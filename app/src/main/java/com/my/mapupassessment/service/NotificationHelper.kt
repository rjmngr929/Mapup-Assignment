package com.my.mapupassessment.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.my.mapupassessment.R

object NotificationHelper {

    private const val CHANNEL_ID = "tracking_channel"
    private const val CHANNEL_NAME = "GPS Tracking"

    fun createNotification(context: Context): Notification {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        // Stop action intent
        val stopIntent = Intent(context, StopTrackingReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("MapUp Tracking Active")
            .setContentText("Your location is being tracked")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }
}