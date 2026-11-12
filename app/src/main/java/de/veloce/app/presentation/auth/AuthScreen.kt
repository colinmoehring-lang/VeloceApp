package de.veloce.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.veloce.app.presentation.theme.VeloceColors

@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isVerificationStep = state.isVerificationStep
    val passwordHints = "Mindestens 8 Zeichen, ein Groß- und Kleinbuchstabe, eine Ziffer und ein Sonderzeichen."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = VeloceColors.Surface,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(VeloceColors.PaleOrange, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.DirectionsBike,
                        contentDescription = "Veloce Logo",
                        tint = VeloceColors.Orange,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when {
                            isVerificationStep -> "E-Mail bestätigen"
                            state.isSignUp -> "Konto erstellen"
                            else -> "Willkommen zurück"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        when {
                            isVerificationStep -> "Gib den 6-stelligen Code aus deiner E-Mail ein."
                            state.isSignUp -> "Erstelle dein Veloce-Konto."
                            else -> "Melde dich an und entdecke deine Fahrten."
                        },
                        color = VeloceColors.Muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.height(2.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("E-Mail") },
                    leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null, tint = VeloceColors.Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                if (isVerificationStep) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { character -> character in '0'..'9' }.take(6) },
                        label = { Text("6-stelliger Verifizierungscode") },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = VeloceColors.Muted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Passwort") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = VeloceColors.Muted) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )
                    if (state.isSignUp) {
                        Text(
                            passwordHints,
                            color = VeloceColors.Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                state.message?.let {
                    Text(it, color = VeloceColors.Green, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        if (isVerificationStep) viewModel.verifyCode(code)
                        else viewModel.submit(password)
                    },
                    enabled = !state.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = VeloceColors.Ink),
                ) {
                    if (state.isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = VeloceColors.Surface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            when {
                                isVerificationStep -> "Code bestätigen"
                                state.isSignUp -> "Konto erstellen"
                                else -> "Anmelden"
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                if (isVerificationStep) {
                    TextButton(
                        onClick = { viewModel.resendVerificationCode(password) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !state.isBusy,
                    ) {
                        Text("Code erneut senden", color = VeloceColors.Muted)
                    }
                }

                TextButton(
                    onClick = viewModel::toggleMode,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !state.isBusy,
                ) {
                    Text(
                        if (state.isSignUp) "Schon ein Konto? Anmelden"
                        else if (isVerificationStep) "Zur Anmeldung"
                        else "Noch kein Konto? Registrieren",
                        color = VeloceColors.Muted,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
