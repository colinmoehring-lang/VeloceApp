package com.veloce.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.ui.theme.CardBg
import com.veloce.app.ui.theme.CardBorder
import com.veloce.app.ui.theme.NeonCyan
import com.veloce.app.ui.theme.NeonPurple

enum class NavTab {
    HOME, RIDE, SETTINGS
}

@Composable
fun CurvedBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = CardBg,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tab: Home
                val homeSelected = currentTab == NavTab.HOME
                val homeScale by animateFloatAsState(if (homeSelected) 1.15f else 1.0f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(homeScale)
                        .clickable { onTabSelected(NavTab.HOME) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (homeSelected) NeonCyan else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 11.sp,
                        fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (homeSelected) NeonCyan else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(60.dp)) // Space for elevated middle FAB

                // Right Tab: Settings
                val settingsSelected = currentTab == NavTab.SETTINGS
                val settingsScale by animateFloatAsState(if (settingsSelected) 1.15f else 1.0f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(settingsScale)
                        .clickable { onTabSelected(NavTab.SETTINGS) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (settingsSelected) NeonCyan else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Settings",
                        fontSize = 11.sp,
                        fontWeight = if (settingsSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (settingsSelected) NeonCyan else Color.Gray
                    )
                }
            }
        }

        // Center Elevated Curved FAB for "Fahrt" / "Ride"
        val rideSelected = currentTab == NavTab.RIDE
        val fabScale by animateFloatAsState(if (rideSelected) 1.15f else 1.0f)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .scale(fabScale)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonCyan, NeonPurple)
                    )
                )
                .clickable { onTabSelected(NavTab.RIDE) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Fahrt",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Fahrt",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
