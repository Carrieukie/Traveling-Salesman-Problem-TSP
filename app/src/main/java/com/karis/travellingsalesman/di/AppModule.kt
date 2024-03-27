package com.karis.travellingsalesman.di

import com.google.maps.GeoApiContext
import com.karis.travellingsalesman.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun providesGeoApiContext(): GeoApiContext {
        return GeoApiContext()
            .setApiKey(
                /* apiKey = */ BuildConfig.MAPS_API_KEY
            )
    }

}