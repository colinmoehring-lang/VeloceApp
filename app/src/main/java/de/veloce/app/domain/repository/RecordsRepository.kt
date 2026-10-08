package de.veloce.app.domain.repository

import de.veloce.app.domain.model.UserRecords

interface RecordsRepository {
    suspend fun getRecords(): UserRecords
}
