package com.karis.travellingsalesman.di

import com.karis.travellingsalesman.data.network.api.PlacesApiService
import com.karis.travellingsalesman.data.network.api.RoutesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun providesOKHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providesConverterFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    @Singleton
    @Provides
    @Named("maps")
    fun providesMapsRetrofit(
        okHttpClient: OkHttpClient,
        converter: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(converter)
            .build()
    }

    @Singleton
    @Provides
    @Named("routes")
    fun providesRoutesRetrofit(
        okHttpClient: OkHttpClient,
        converter: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://routes.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(converter)
            .build()
    }


    @Singleton
    @Provides
    fun providesPlacesApiService(
        @Named("maps") retrofit: Retrofit
    ): PlacesApiService {
        return retrofit.create(PlacesApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesRoutesApiService(
        @Named("routes") retrofit: Retrofit
    ): RoutesApiService {
        return retrofit.create(RoutesApiService::class.java)
    }

}