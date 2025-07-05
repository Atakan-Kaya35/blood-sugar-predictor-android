package com.example.bspapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import com.example.bspapp.util.postRequest
import androidx.compose.ui.text.input.PasswordVisualTransformation


@Composable
fun StartTrainingScreen(
    navController: NavController,
    usernameInit: String,
    passwordInit: String
) {
    var username by remember { mutableStateOf(usernameInit) }
    var password by remember { mutableStateOf(passwordInit) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    fun fetchAndTrain() {
        coroutineScope.launch {
            loading = true
            message = ""

            try {
                // Step 1: Fetch Dexcom data via App Runner
                val fetchResponse = postRequest(
                    "https://ejfh8eyg6j.eu-central-1.awsapprunner.com/run/$username/$password"
                )
                if (!fetchResponse.first) throw Exception(fetchResponse.second)
                message = "Dexcom data fetched, now starting training..."

                // Step 2: Trigger training Lambda
                val trainingBody = JSONObject().apply {
                    put("username", username)
                    put("num_of_models", 2)
                    put("epochs", 7)
                    put("batch_size", 24)
                    put("remaining_tries", 2)
                    put("acceptable_acc_score", 0.1)
                    put("seq_len", 12)
                    put("num_of_layers", 3)
                }

                val trainResponse = postRequest(
                    "https://7gh3eu50xc.execute-api.eu-central-1.amazonaws.com/dev/training_job_trigger",
                    trainingBody.toString()
                )
                if (!trainResponse.first) throw Exception(trainResponse.second)

                message = "Training successfully triggered!"
                navController.navigate("predict/$username/$password")

            } catch (e: Exception) {
                message = e.message ?: "Error"
            } finally {
                loading = false
            }
        }
    }

    fun trainFromExistingData() {
        coroutineScope.launch {
            loading = true
            message = ""

            try {
                val body = JSONObject().apply {
                    put("username", username)
                    put("num_of_models", 2)
                    put("epochs", 7)
                    put("batch_size", 24)
                    put("remaining_tries", 2)
                    put("acceptable_acc_score", 0.1)
                    put("seq_len", 12)
                    put("num_of_layers", 5)
                }

                val response = postRequest(
                    "https://7gh3eu50xc.execute-api.eu-central-1.amazonaws.com/dev/training_job_trigger",
                    body.toString()
                )
                if (!response.first) throw Exception(response.second)

                message = "Training started from existing data!"
                navController.navigate("predict/$username/$password")

            } catch (e: Exception) {
                message = e.message ?: "Error"
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Start Model Training", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { fetchAndTrain() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Working..." else "Fetch Dexcom + Start Training")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { trainFromExistingData() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Starting..." else "Start From Existing Data")
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
