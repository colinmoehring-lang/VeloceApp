package com.veloce.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.veloce.app.data.bluetooth.BluetoothRepositoryImpl
import com.veloce.app.data.repository_impl.AuthRepositoryImpl
import com.veloce.app.data.repository_impl.RideRepositoryImpl
import com.veloce.app.data.repository_impl.UserRepositoryImpl
import com.veloce.app.data.repository_impl.VehicleRepositoryImpl
import com.veloce.app.presentation.*
import com.veloce.app.ui.components.CurvedBottomBar
import com.veloce.app.ui.components.NavTab
import com.veloce.app.ui.screens.*
import com.veloce.app.ui.theme.VeloceTheme

sealed class Screen {
    object Auth : Screen()
    object Main : Screen()
    object Vehicles : Screen()
    object RidesHistory : Screen()
    data class RideDetail(val rideId: String) : Screen()
}

@Composable
fun App() {
    // Instantiate Dependency Injection / Repositories
    val authRepo = remember { AuthRepositoryImpl() }
    val userRepo = remember { UserRepositoryImpl() }
    val vehicleRepo = remember { VehicleRepositoryImpl() }
    val rideRepo = remember { RideRepositoryImpl() }
    val bluetoothRepo = remember { BluetoothRepositoryImpl() }

    // ViewModels
    val authViewModel = remember { AuthViewModel(authRepo) }
    val homeViewModel = remember { HomeViewModel(userRepo, vehicleRepo, rideRepo) }
    val rideViewModel = remember { RideViewModel(rideRepo, bluetoothRepo) }
    val rideDetailViewModel = remember { RideDetailViewModel(rideRepo) }
    val vehicleViewModel = remember { VehicleViewModel(vehicleRepo) }
    val settingsViewModel = remember { SettingsViewModel(bluetoothRepo) }

    val authState by authViewModel.state.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(if (authState.isLoggedIn) Screen.Main else Screen.Auth) }
    var currentNavTab by remember { mutableStateOf(NavTab.HOME) }

    LaunchedEffect(authState.isLoggedIn) {
        currentScreen = if (authState.isLoggedIn) Screen.Main else Screen.Auth
    }

    VeloceTheme {
        when (val screen = currentScreen) {
            is Screen.Auth -> {
                AuthScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { currentScreen = Screen.Main }
                )
            }
            is Screen.Main -> {
                val vehicleState by vehicleViewModel.state.collectAsState()
                Scaffold(
                    bottomBar = {
                        CurvedBottomBar(
                            currentTab = currentNavTab,
                            onTabSelected = { tab -> currentNavTab = tab }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentNavTab) {
                            NavTab.HOME -> HomeScreen(
                                homeViewModel = homeViewModel,
                                onNavigateToVehicles = { currentScreen = Screen.Vehicles },
                                onNavigateToRides = { currentScreen = Screen.RidesHistory },
                                onSelectRideDetail = { id -> currentScreen = Screen.RideDetail(id) }
                            )
                            NavTab.RIDE -> RideScreen(
                                rideViewModel = rideViewModel,
                                userVehicles = vehicleState.vehicles
                            )
                            NavTab.SETTINGS -> SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = Screen.Auth
                                }
                            )
                        }
                    }
                }
            }
            is Screen.Vehicles -> {
                VehiclesScreen(
                    vehicleViewModel = vehicleViewModel,
                    onBack = { currentScreen = Screen.Main }
                )
            }
            is Screen.RidesHistory -> {
                RidesHistoryScreen(
                    homeViewModel = homeViewModel,
                    onSelectRideDetail = { id -> currentScreen = Screen.RideDetail(id) },
                    onBack = { currentScreen = Screen.Main }
                )
            }
            is Screen.RideDetail -> {
                RideDetailScreen(
                    rideId = screen.rideId,
                    rideDetailViewModel = rideDetailViewModel,
                    onBack = { currentScreen = Screen.Main }
                )
            }
        }
    }
}
