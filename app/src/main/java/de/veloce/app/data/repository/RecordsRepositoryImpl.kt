package de.veloce.app.data.repository

import de.veloce.app.data.mapper.toDomain
import de.veloce.app.data.remote.VeloceApi
import de.veloce.app.data.remote.toApiException
import de.veloce.app.domain.model.UserRecords
import de.veloce.app.domain.repository.RecordsRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsRepositoryImpl @Inject constructor(
    private val api: VeloceApi,
) : RecordsRepository {
    override suspend fun getRecords(): UserRecords = try {
        api.getUserRecords().toDomain()
    } catch (exception: HttpException) {
        throw exception.toApiException()
    }
}
