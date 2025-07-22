package com.ataka.bspapp.data

import android.content.Context
import android.util.Log
import com.ataka.bspapp.util.postRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object PredictionManager {
    suspend fun fetchLatestInference(username: String, password: String, context: Context): Boolean {
        return try {
            val body = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            val (success, responseText) = withContext(Dispatchers.IO) {
                postRequest(
                    "https://7gh3eu50xc.execute-api.eu-central-1.amazonaws.com/dev/inference",
                    body.toString()
                )
            }

            if (!success) throw Exception("Request failed: $responseText")

            context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
                .edit().putString("credentials", body.toString()).apply()

            PredictionRepository.updatePredictions(responseText)
            Log.d("BSP", "📢 Updated repository with: $responseText")
            true
        } catch (e: Exception) {
            Log.e("BSP", "❌ Foreground fetch failed", e)
            false
        }
    }
}
