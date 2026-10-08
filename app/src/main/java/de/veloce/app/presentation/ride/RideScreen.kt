package de.veloce.app.presentation.ride

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.veloce.app.presentation.theme.VeloceColors

@Composable
fun RideScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Fahrt", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Die Fahrtaufzeichnung wird später verfügbar sein.",
            modifier = Modifier.padding(top = 8.dp),
            color = VeloceColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}
