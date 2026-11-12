package de.veloce.app.data.repository

import de.veloce.app.data.remote.VeloceApi
import de.veloce.app.data.remote.toApiException
import de.veloce.app.data.mapper.toDomain
import de.veloce.app.data.mapper.toMapPointOrNull
import de.veloce.app.data.remote.dto.AddRidePointRequestDto
import de.veloce.app.data.remote.dto.StartRideRequestDto
import de.veloce.app.domain.model.MapPoint
import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.model.TelemetryPoint
import de.veloce.app.domain.repository.RideRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val api: VeloceApi,
) : RideRepository {
    override suspend fun startRide(vehicleType: String, vehicleId: String?): String = callApi {
        api.startRide(StartRideRequestDto(vehicleType, vehicleId)).rideId
    }

    override suspend fun addPoint(rideId: String, point: TelemetryPoint) {
        require(point.hasValidCoordinates) { "Punkte ohne GPS-Koordinaten dürfen nicht übertragen werden." }
        callApi {
            requireSuccess(
                api.addRidePoint(
                    rideId,
                    AddRidePointRequestDto(
                        timestamp = point.timestamp.toString(),
                        coordinate = String.format(
                            Locale.ROOT,
                            "%.6f,%.6f",
                            point.latitude,
                            point.longitude,
                        ),
                        speed = point.speedKmh,
                        gForce = point.gForce,
                        lean = point.leanDegrees,
                    ),
                ),
            )
        }
    }

    override suspend fun stopRide(rideId: String) {
        callApi { requireSuccess(api.stopRide(rideId)) }
    }

    override suspend fun getRides(): List<RideHistoryItem> = callApi {
        api.getRides()
            .map { it.toDomain() }
            .sortedByDescending { it.startedAt }
    }

    override suspend fun getRideRoute(rideId: String): List<MapPoint> = callApi {
        api.getRidePointCoordinates(rideId).mapNotNull(String::toMapPointOrNull)
    }

    private fun requireSuccess(response: Response<Unit>) {
        if (!response.isSuccessful) throw HttpException(response)
    }

    private suspend fun <T> callApi(block: suspend () -> T): T = try {
        block()
    } catch (exception: HttpException) {
        throw exception.toApiException()
    }
}