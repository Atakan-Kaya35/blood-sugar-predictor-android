package com.example.bspapp.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bspapp.R
import com.example.bspapp.util.postRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BspFetchWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notifEnabled = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
                .getBoolean("notifications_enabled", true)

            if (!notifEnabled) return@withContext Result.success()
            else{
                val prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
                val rawCreds = prefs.getString("credentials", null)
                if (rawCreds.isNullOrEmpty()) return@withContext Result.failure()

                val (username, password) = JSONObject(rawCreds).let {
                    it.getString("username") to it.getString("password")
                }

                val body = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                val (success, response) = postRequest(
                    "https://7gh3eu50xc.execute-api.eu-central-1.amazonaws.com/dev/inference",
                    body.toString()
                )

                if (!success) return@withContext Result.retry()

                val json = JSONObject(response)
                val safe = json.optBoolean("safeness", true)
                val trend = json.optInt("trend", 56)
                val indicators = json.optInt("indicators", 0)
                val trendDir = trend % 8
                val (extreme, plateau, trendChange) = indicators.toString().padStart(3, '0').map { it.toString().toInt() }

                if (!safe) notify("🚨 Blood Sugar Alert", "Your levels are out of the safe range.")
                if (trendDir <= 2) notify("⚠️ Rapid Drop", "You're trending down fast. Consider action.")
                if (extreme != 0) notify("📛 Extreme Prediction", "Very high/low blood sugar expected soon.")
                if (plateau == 1) notify("⏸️ Plateau Expected", "Glucose may stabilize. Monitor it.")
                if (trendChange != 0) notify("🔄 Trend Change", "Upcoming trend shift detected.")

                Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun notify(title: String, text: String) {
        val channelId = "bsp_notifs"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "BSP Notifications", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // your app icon
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify((System.currentTimeMillis() % 10000).toInt(), notif)
    }
}
