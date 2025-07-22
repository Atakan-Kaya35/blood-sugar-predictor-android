package com.ataka.bspapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ataka.bspapp.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun LoginScreen(navController: NavController) {
    // for toolbar writing color
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = true // dark icons on light background

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = androidx.compose.ui.graphics.Color.White,
            darkIcons = useDarkIcons
        )
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF) // Off-white
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.bsp_logo),
                contentDescription = "BSP Logo",
                modifier = Modifier
                    .height(200.dp)
                    .padding(16.dp)
            )

            //Text("BSP Login", style = MaterialTheme.typography.headlineMedium)

            //Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    loading = true
                    message = ""
                    navController.navigate("predict/${username}/${password}")
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Logging in..." else "Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("startTraining/${username}/${password}")
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Signing up..." else "Sign Up")
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
