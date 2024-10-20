package com.phone.tracker.di

import android.app.Application
import android.content.Context
import com.phone.tracker.data.api.ApiConstants
import com.phone.tracker.data.api.ApiEndPoints
import com.phone.tracker.data.local.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(builder: Retrofit.Builder): ApiEndPoints {
        return builder
            .build()
            .create(ApiEndPoints::class.java)

    }

    /**
     * Create retrofit object
     */
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit.Builder {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL).client(
                client
            )
            .addConverterFactory(GsonConverterFactory.create())
    }


    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun providePreferencesManager(context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

}