package com.example.bspapp

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bspapp.ui.theme.BSPAppTheme

import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.bspapp.ui.LoginScreen
import com.example.bspapp.ui.StartTrainingScreen
import com.example.bspapp.ui.PredictionScreen

import com.github.mikephil.charting.utils.Utils

import androidx.work.*
import com.example.bspapp.background.BspFetchWorker
import com.example.bspapp.util.AppStateMonitor
import com.example.bspapp.util.createNotificationChannel

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