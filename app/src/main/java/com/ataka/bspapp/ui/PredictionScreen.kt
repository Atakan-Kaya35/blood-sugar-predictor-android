package com.ataka.bspapp.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ataka.bspapp.util.postRequest
import kotlinx.coroutines.*
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.SystemUiController
import androidx.compose.runtime.livedata.observeAsState
import com.ataka.bspapp.data.PredictionManager
import com.ataka.bspapp.data.PredictionRepository

@Composable
fun PredictionScreen(navController: NavController, username: String, password: String) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = true

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = androidx.compose.ui.graphics.Color.White,
            darkIcons = useDarkIcons
        )
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("notifications_enabled", true))
    }

    // Observe global prediction data (raw JSON string)
    val predictionString by PredictionRepository.predictions.observeAsState("")

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var predictionData by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var indicators by remember { mutableStateOf(0) }

    // Parse the latest prediction string when it changes
    LaunchedEffect(predictionString) {
        Log.d("BSP", "🔍 Observed predictionString change 1: $predictionString")
        if (predictionString.isNotEmpty()) {
            try {
                val response = JSONObject(predictionString)
                val fullList = extractTriplets(response.getInt("befores2")) +
                        extractTriplets(response.getInt("befores1")) +
                        extractTriplets(response.getInt("befores")) +
                        extractTriplets(response.getInt("afters"))

                indicators = response.getInt("indicators")
                predictionData = fullList
                error = null
                loading = false
            } catch (e: Exception) {
                Log.e("BSP", "❌ Failed to parse prediction", e)
                error = "Parse error: ${e.message ?: "Unknown"}"
            }
        }
    }

    // Lifecycle awareness for 5-minute refresh
    val lifecycleOwner = LocalLifecycleOwner.current
    var isActive by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isActive = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun fetchPrediction() {
        coroutineScope.launch {
            loading = true
            val success = PredictionManager.fetchLatestInference(username, password, context)
            if (!success) {
                error = "Prediction failed: see logs"
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("BSP", "🔍 Observed predictionString change: $predictionString")
        fetchPrediction() // initial fetch
        while (true) {
            delay(5 * 60 * 1000L) // 5 minutes
            if (isActive) {
                Log.d("BSP", "⏱ Triggering periodic prediction refresh")
                fetchPrediction()
            }
        }
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
        predictionData.isNotEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    PredictionChart(values = predictionData, indicators = indicators)
                }
            }
        }
    }
}

// Helper to break down numeric values
fun extractTriplets(number: Int): List<Pair<Float, Float>> {
    val a = (number / 1_000_000) % 1000
    val b = (number / 1_000) % 1000
    val c = number % 1000
    return listOf(a, b, c).map {
        val clamped = it.coerceIn(30, 300)
        clamped.toFloat() to 0.5f
    }
}

private fun SystemUiController.setSystemBarsColor(color: Int, darkIcons: Boolean) {
    return
}
