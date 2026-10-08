package de.veloce.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.veloce.app.presentation.theme.VeloceColors

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Start", Icons.Outlined.Home),
    Ride("ride", "Fahrt", Icons.Outlined.DirectionsBike),
    Settings("settings", "Einstellungen", Icons.Outlined.Settings),
}

@Composable
fun FloatingBottomBar(
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(12.dp, CircleShape, ambientColor = Color(0x1A000000)),
        shape = CircleShape,
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = selectedRoute == tab.route
                Row(
                    modifier = Modifier
                        .background(
                            if (selected) VeloceColors.Ink else Color.Transparent,
                            CircleShape,
                        )
                        .clickable { onSelect(tab.route) }
                        .padding(horizontal = if (selected) 15.dp else 12.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) Color.White else VeloceColors.Muted,
                    )
                    if (selected) Text(tab.label, color = Color.White)
                }
            }
        }
    }
}
