package com.example.bspapp.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

suspend fun postRequest(urlStr: String, body: String = ""): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("BSP", "🔁 Opening connection to $urlStr")
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            Log.d("BSP", "📦 Sending body: $body")
            val outputStream = conn.outputStream
            val writer = OutputStreamWriter(outputStream)
            writer.write(body)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            val responseMessage = conn.inputStream.bufferedReader().readText()

            Log.d("BSP", "📩 HTTP $responseCode response: $responseMessage")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Pair(true, responseMessage)
            } else {
                Pair(false, "HTTP error: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("BSP", "🔥 Exception during postRequest: ${e.message}")
            Pair(false, "EXCEPTION: ${e.message}")
        }
    }
}