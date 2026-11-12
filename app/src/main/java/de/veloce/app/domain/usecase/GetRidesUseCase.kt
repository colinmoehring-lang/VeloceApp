package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.RideHistoryItem
import de.veloce.app.domain.repository.RideRepository

class GetRidesUseCase(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(): List<RideHistoryItem> = repository.getRides()
}