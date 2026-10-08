package de.veloce.app.di

import de.veloce.app.data.repository.AuthRepositoryImpl
import de.veloce.app.data.repository.RecordsRepositoryImpl
import de.veloce.app.data.repository.RideRepositoryImpl
import de.veloce.app.data.simulator.SimulatedTelemetrySource
import de.veloce.app.data.repository.VehicleRepositoryImpl
import de.veloce.app.domain.repository.AuthRepository
import de.veloce.app.domain.repository.RecordsRepository
import de.veloce.app.domain.repository.RideRepository
import de.veloce.app.domain.repository.TelemetrySource
import de.veloce.app.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindVehicleRepository(implementation: VehicleRepositoryImpl): VehicleRepository

    @Binds
    abstract fun bindRecordsRepository(implementation: RecordsRepositoryImpl): RecordsRepository

    @Binds
    abstract fun bindRideRepository(implementation: RideRepositoryImpl): RideRepository

    @Binds
    abstract fun bindTelemetrySource(implementation: SimulatedTelemetrySource): TelemetrySource
}
