package com.ataka.bspapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.ataka.bspapp.ui.theme.BSPAppTheme

import androidx.navigation.compose.*
import com.ataka.bspapp.ui.LoginScreen
import com.ataka.bspapp.ui.StartTrainingScreen
import com.ataka.bspapp.ui.PredictionScreen

import com.github.mikephil.charting.utils.Utils

import androidx.work.*
import com.ataka.bspapp.background.BspFetchWorker
import com.ataka.bspapp.data.PredictionManager
import com.ataka.bspapp.util.AppStateMonitor
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // init function to create a channel for in app notifications
        //createNotificationChannel(this)

        // ✅ Init MPAndroidChart for the graphing lib
        Utils.init(this)

        // for app state (foreground vs. background monitoring)
        AppStateMonitor.initialize(application)

        AppStateMonitor.setOnForegroundCallback {
            // Fresh inference request
            fetchLatestInference()
        }

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(navController)
                }
                composable("startTraining/{username}/{password}") { backStack ->
                    val username = backStack.arguments?.getString("username") ?: ""
                    val password = backStack.arguments?.getString("password") ?: ""
                    StartTrainingScreen(navController, username, password)
                }
                composable("predict/{username}/{password}") { backStack ->
                    val username = backStack.arguments?.getString("username") ?: ""
                    val password = backStack.arguments?.getString("password") ?: ""
                    PredictionScreen(navController, username, password)
                }
            }
        }

        val workRequest = PeriodicWorkRequestBuilder<BspFetchWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bsp_bg_fetch",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

    }

    private fun fetchLatestInference() {
        val prefs = getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE)
        val creds = prefs.getString("credentials", null)

        if (creds != null) {
            try {
                val json = JSONObject(creds)
                val username = json.getString("username")
                val password = json.getString("password")

                lifecycleScope.launch {
                    Log.d("BSP", "🔄 Fetching fresh inference (foreground resume)")
                    PredictionManager.fetchLatestInference(username, password, this@MainActivity)
                }
            } catch (e: Exception) {
                Log.e("BSP", "❌ Failed to parse stored credentials", e)
            }
        } else {
            Log.w("BSP", "⚠ No saved credentials for foreground fetch")
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BSPAppTheme {
        Greeting("Android")
    }
}