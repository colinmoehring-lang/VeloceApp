package de.veloce.app.presentation.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.veloce.app.presentation.components.VeloceCard
import de.veloce.app.presentation.theme.VeloceColors

@Composable
fun RideListScreen(
    viewModel: RideListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VeloceColors.Background),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Fahrten", style = MaterialTheme.typography.headlineMedium)
        }

        if (state.isLoading && state.rides.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.error != null && state.rides.isEmpty()) {
            item {
                VeloceCard {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(onClick = viewModel::refresh) { Text("Erneut laden") }
                    }
                }
            }
        } else if (state.rides.isEmpty()) {
            item {
                VeloceCard {
                    Text(
                        text = "Noch keine Fahrten aufgezeichnet.",
                        modifier = Modifier.padding(18.dp),
                        color = VeloceColors.Muted,
                    )
                }
            }
        } else {
            items(state.rides, key = { it.id }) { ride ->
                val isExpanded = state.expandedRideId == ride.id
                RideHistoryCard(
                    ride = ride,
                    route = if (isExpanded) state.expandedRoute else emptyList(),
                    isRouteLoading = isExpanded && state.isRouteLoading,
                    routeError = if (isExpanded) state.routeError else null,
                    showRoute = isExpanded,
                    onToggleRoute = { viewModel.toggleRoute(ride.id) },
                )
            }
        }
    }
}
