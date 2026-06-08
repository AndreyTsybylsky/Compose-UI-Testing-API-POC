package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                var isLoggedIn by remember { mutableStateOf(false) }
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf("") }

                if (!isLoggedIn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().testTag("login_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Welcome to KMP Testing",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            OutlinedTextField(
                                value = username,
                                onValueChange = { 
                                    username = it
                                    errorMessage = ""
                                },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth().testTag("username_input")
                            )
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { 
                                    password = it
                                    errorMessage = ""
                                },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth().testTag("password_input")
                            )

                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    modifier = Modifier.testTag("error_text")
                                )
                            }

                            Button(
                                onClick = {
                                    if (username.length < 4) {
                                        errorMessage = "Username must be at least 4 characters"
                                    } else if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                    } else {
                                        isLoggedIn = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("login_button")
                            ) {
                                Text("Login")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Render native banner (which can be overridden in tests)
                            PlatformProvider.RenderNativeBanner(
                                modifier = Modifier.fillMaxWidth().testTag("native_banner")
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().testTag("dashboard_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Hello, $username!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.testTag("welcome_text")
                            )
                            
                            Text(
                                text = "You have successfully authenticated via Compose Multiplatform UI.",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = {
                                    isLoggedIn = false
                                    password = ""
                                },
                                modifier = Modifier.fillMaxWidth().testTag("logout_button")
                            ) {
                                Text("Log Out")
                            }
                        }
                    }
                }
            }
        }
    }
}
