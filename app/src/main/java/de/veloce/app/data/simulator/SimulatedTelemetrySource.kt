package de.veloce.app.data.simulator

import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.repository.TelemetrySource
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class SimulatedTelemetrySource @Inject constructor() : TelemetrySource {
    private val route = listOf(
        48.5216 to 9.0576,
        48.5219 to 9.0601,
        48.5230 to 9.0610,
        48.5246 to 9.0603,
        48.5252 to 9.0578,
        48.5244 to 9.0555,
        48.5227 to 9.0551,
    )
    private val sampledRoute = buildList {
        route.forEachIndexed { index, start ->
            val end = route[(index + 1) % route.size]
            val distanceMeters = haversineMeters(start, end)
            val steps = (distanceMeters / 11.1).toInt().coerceAtLeast(1)
            for (step in 0 until steps) {
                val fraction = step.toDouble() / steps
                add(
                    (start.first + (end.first - start.first) * fraction) to
                        (start.second + (end.second - start.second) * fraction),
                )
            }
        }
    }

    override fun points(): Flow<TelemetryPoint> = flow {
        var index = 0
        while (true) {
            val phase = index * 2.0 * PI / 24.0
            val coordinate = sampledRoute[index % sampledRoute.size]
            emit(
                TelemetryPoint(
                    timestamp = Instant.now(),
                    latitude = coordinate.first,
                    longitude = coordinate.second,
                    speedKmh = 40.0 + 1.5 * sin(phase),
                    leanDegrees = 7.0 * sin(phase * 0.7),
                    gForce = 0.15 + 0.05 * sin(phase * 1.2),
                ),
            )
            index++
            delay(1_000L)
        }
    }

    private fun haversineMeters(
        start: Pair<Double, Double>,
        end: Pair<Double, Double>,
    ): Double {
        val latitudeDelta = Math.toRadians(end.first - start.first)
        val longitudeDelta = Math.toRadians(end.second - start.second)
        val a = kotlin.math.sin(latitudeDelta / 2).let { it * it } +
            kotlin.math.cos(Math.toRadians(start.first)) *
            kotlin.math.cos(Math.toRadians(end.first)) *
            kotlin.math.sin(longitudeDelta / 2).let { it * it }
        return 6_371_000.0 * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}