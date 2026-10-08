package de.veloce.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.veloce.app.domain.repository.AuthRepository
import de.veloce.app.domain.repository.RecordsRepository
import de.veloce.app.domain.repository.RideRepository
import de.veloce.app.domain.repository.VehicleRepository
import de.veloce.app.domain.usecase.CreateVehicleUseCase
import de.veloce.app.domain.usecase.DeleteVehicleUseCase
import de.veloce.app.domain.usecase.GetPersonalRecordsUseCase
import de.veloce.app.domain.usecase.GetVehiclesUseCase
import de.veloce.app.domain.usecase.LoginUseCase
import de.veloce.app.domain.usecase.RecordRideUseCase
import de.veloce.app.domain.usecase.SignUpUseCase
import de.veloce.app.domain.usecase.StartRideUseCase
import de.veloce.app.domain.usecase.StopRideUseCase
import de.veloce.app.domain.usecase.UpdateVehicleUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideLoginUseCase(repository: AuthRepository) = LoginUseCase(repository)

    @Provides
    fun provideSignUpUseCase(repository: AuthRepository) = SignUpUseCase(repository)

    @Provides
    fun provideCreateVehicleUseCase(repository: VehicleRepository) = CreateVehicleUseCase(repository)

    @Provides
    fun provideUpdateVehicleUseCase(repository: VehicleRepository) = UpdateVehicleUseCase(repository)

    @Provides
    fun provideDeleteVehicleUseCase(repository: VehicleRepository) = DeleteVehicleUseCase(repository)

    @Provides
    fun provideGetVehiclesUseCase(repository: VehicleRepository) = GetVehiclesUseCase(repository)

    @Provides
    fun provideGetRecordsUseCase(repository: RecordsRepository) = GetPersonalRecordsUseCase(repository)

    @Provides
    fun provideStartRideUseCase(repository: RideRepository) = StartRideUseCase(repository)

    @Provides
    fun provideRecordRideUseCase(repository: RideRepository) = RecordRideUseCase(repository)

    @Provides
    fun provideStopRideUseCase(repository: RideRepository) = StopRideUseCase(repository)
}
