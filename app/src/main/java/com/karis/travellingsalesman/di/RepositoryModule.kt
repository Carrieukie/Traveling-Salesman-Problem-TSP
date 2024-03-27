package com.karis.travellingsalesman.di

import com.karis.travellingsalesman.data.repository.DistanceMatrixRepositoryImpl
import com.karis.travellingsalesman.data.repository.PlacesRepositoryImpl
import com.karis.travellingsalesman.data.repository.PolyLinesRepositoryImpl
import com.karis.travellingsalesman.domain.repository.DistanceMatrixRepository
import com.karis.travellingsalesman.domain.repository.PlacesRepository
import com.karis.travellingsalesman.domain.repository.PolylinesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun providesComputeRouteRepository(
        computeRouteRepositoryImpl: DistanceMatrixRepositoryImpl
    ): DistanceMatrixRepository

    @Binds
    @Singleton
    abstract fun providesPlacesRepository(
        placesRepositoryImpl: PlacesRepositoryImpl
    ): PlacesRepository

    @Binds
    @Singleton
    abstract fun providesPolylinesRepository(
        polylinesRepositoryImpl: PolyLinesRepositoryImpl
    ): PolylinesRepository
}