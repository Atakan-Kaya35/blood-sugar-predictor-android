package com.example.bspapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bspapp.util.postRequest
import kotlinx.coroutines.*
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.bspapp.util.triggerTestNotification


@Composable
fun PredictionScreen(navController: NavController, username: String, password: String) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var predictionData by remember { mutableStateOf<List<Pair<Float, Float>>?>(null) }
    var indicators by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("notifications_enabled", true))
    }

    fun extractTriplets(number: Int): List<Pair<Float, Float>> {
        val a = (number / 1_000_000) % 1000
        val b = (number / 1_000) % 1000
        val c = number % 1000
        return listOf(a, b, c).map {
            // ✅ Clamp values to safe range for rendering
            val clamped = it.coerceIn(30, 300)
            clamped.toFloat() to 0.5f
        }
    }

    fun fetchPrediction() {
        coroutineScope.launch {
            Log.d("BSP", "🚀 Sending prediction request for $username")
            try {
                val body = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                Log.d("BSP", "REQUEST BODY: $body")

                val (success, responseText) = withContext(Dispatchers.IO) {
                    postRequest(
                        "https://7gh3eu50xc.execute-api.eu-central-1.amazonaws.com/dev/inference",
                        body.toString()
                    )
                }

                Log.d("BSP", "RESPONSE: $responseText")

                if (!success) throw Exception("Request failed: $responseText")

                // save preferences to contxt after making sure call successful
                prefs.edit().putString("credentials", body.toString()).apply()

                val response = JSONObject(responseText)
                val fullList = extractTriplets(response.getInt("befores2")) +
                        extractTriplets(response.getInt("befores1")) +
                        extractTriplets(response.getInt("befores")) +
                        extractTriplets(response.getInt("afters"))

                indicators = response.getInt("indicators")
                predictionData = fullList
                error = null
            } catch (e: Exception) {
                Log.e("BSP", "❌ Failed to fetch prediction", e)
                error = "Prediction failed: ${e.message ?: "Unknown"}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchPrediction()
    }

    when {
        loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading predictions...", style = MaterialTheme.typography.bodyLarge)
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }
        }
        predictionData != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)

            ) {
                // Title
                //Text( text = "BSP Prediction Chart", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp, top = 50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notifications", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            prefs.edit().putBoolean("notifications_enabled", it).apply()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))

                // TEST Notification, a button sending local notifications
                /*Button(
                    onClick = { triggerTestNotification(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Send Test Notification")
                }*/

                // Chart at bottom, fills remaining space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    PredictionChart(values = predictionData ?: emptyList(), indicators = indicators)
                }
            }
        }
    }
}
