package de.veloce.app.domain.usecase

import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.repository.RecordsRepository

class GetPersonalRecordsUseCase(
    private val repository: RecordsRepository,
) {
    suspend operator fun invoke(): UserRecords = repository.getRecords()
}
