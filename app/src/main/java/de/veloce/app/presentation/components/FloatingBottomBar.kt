package de.veloce.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        modifier = modifier.shadow(
            elevation = 16.dp,
            shape = CircleShape,
            ambientColor = Color(0x20000000),
            spotColor = Color(0x1A000000),
        ),
        shape = CircleShape,
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = selectedRoute == tab.route
                val pillColor by animateColorAsState(
                    targetValue = if (selected) VeloceColors.Ink else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tabPillColor",
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) Color.White else VeloceColors.Muted,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tabTextColor",
                )

                Row(
                    modifier = Modifier
                        .background(pillColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(tab.route) }
                        .padding(horizontal = if (selected) 18.dp else 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = textColor,
                    )
                    if (selected) {
                        Text(
                            text = tab.label,
                            color = textColor,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }
    }
}
