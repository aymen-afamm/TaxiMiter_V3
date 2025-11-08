package com.taximeter.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.taximeter.app.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "taxi_meter_channel"
        const val CHANNEL_NAME = "Taxi Meter Notifications"
        const val TRIP_END_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour le compteur de taxi"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTripEndNotification(distance: Double, duration: Double, fare: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taxi)
            .setContentTitle("Course Terminée")
            .setContentText("Montant: ${String.format("%.2f", fare)} DH")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Distance: ${String.format("%.2f", distance)} km\n" +
                        "Durée: ${String.format("%.0f", duration)} min\n" +
                        "Montant: ${String.format("%.2f", fare)} DH"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(TRIP_END_NOTIFICATION_ID, notification)
    }
}
