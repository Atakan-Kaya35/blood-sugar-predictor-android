package com.example.bspapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

fun triggerTestNotification(context: Context) {
    val channelId = "bsp_channel"

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Test Notification")
        .setContentText("This is a test BSP notification.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
    notificationManager?.notify(1001, builder.build())
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "bsp_channel",
            "BSP Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for BSP predictions and alerts"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
