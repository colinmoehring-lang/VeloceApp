package de.veloce.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.veloce.app.presentation.auth.AuthScreen
import de.veloce.app.presentation.auth.AuthViewModel
import de.veloce.app.presentation.components.FloatingBottomBar
import de.veloce.app.presentation.components.MainTab
import de.veloce.app.presentation.home.HomeScreen
import de.veloce.app.presentation.home.HomeViewModel
import de.veloce.app.presentation.rides.RideListScreen
import de.veloce.app.presentation.ride.RideScreen
import de.veloce.app.presentation.settings.SettingsScreen
import de.veloce.app.presentation.theme.VeloceColors
import de.veloce.app.presentation.vehicles.VehicleListScreen

@Composable
fun VeloceNavHost() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()
    if (authState.isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VeloceColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    LaunchedEffect(authState.session) {
        val destination = if (authState.session == null) Routes.Login else Routes.Home
        if (currentRoute != null && currentRoute != destination) {
            navController.navigate(destination) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val selectedTab = MainTab.entries.firstOrNull { it.route == currentRoute }
    Scaffold(
        containerColor = VeloceColors.Background,
        bottomBar = {
            if (selectedTab != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    FloatingBottomBar(
                        selectedRoute = selectedTab.route,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.Home) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (authState.session == null) Routes.Login else Routes.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Login) {
                AuthScreen(viewModel = authViewModel)
            }
            composable(Routes.Home) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    onOpenVehicles = { navController.navigate(Routes.Vehicles) },
                    onOpenRides = { navController.navigate(Routes.RideList) },
                    onLogout = authViewModel::logOut,
                    viewModel = homeViewModel,
                )
            }
            composable(Routes.Vehicles) {
                val homeEntry = remember(navController) { navController.getBackStackEntry(Routes.Home) }
                val homeViewModel: HomeViewModel = hiltViewModel(homeEntry)
                VehicleListScreen(
                    onBack = { navController.popBackStack() },
                    onVehiclesChanged = homeViewModel::refresh,
                )
            }
            composable(Routes.Ride) {
                RideScreen(onOpenSettings = { navController.navigate(Routes.Settings) })
            }
            composable(Routes.RideList) { RideListScreen() }
            composable(Routes.Settings) {
                SettingsScreen(onLogout = authViewModel::logOut)
            }
        }
    }
}
