package com.veloce.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.presentation.AuthViewModel
import com.veloce.app.ui.theme.CardBg
import com.veloce.app.ui.theme.DarkBg
import com.veloce.app.ui.theme.NeonCyan
import com.veloce.app.ui.theme.NeonPurple

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by authViewModel.state.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Text(
                    text = "VELOCE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Telemetry & BoardComputer",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isSignUp = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSignUp) NeonCyan else Color.Transparent,
                            contentColor = if (!isSignUp) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Anmelden", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isSignUp = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignUp) NeonCyan else Color.Transparent,
                            contentColor = if (isSignUp) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Registrieren", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message
                state.error?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Success Message
                state.message?.let { msg ->
                    Text(
                        text = msg,
                        color = NeonCyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        if (isSignUp) {
                            authViewModel.signUp(username, password)
                        } else {
                            authViewModel.login(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text(
                            text = if (isSignUp) "KONTO ERSTELLEN" else "JETZT ANMELDEN",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
