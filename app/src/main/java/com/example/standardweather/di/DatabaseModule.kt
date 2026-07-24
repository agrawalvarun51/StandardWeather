package com.example.standardweather.di

import android.content.Context
import androidx.room.Room
import com.example.standardweather.data.local.WeatherDatabase
import com.example.standardweather.data.local.dao.SearchHistoryDao
import com.example.standardweather.data.local.dao.WeatherCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.databaseBuilder(context, WeatherDatabase::class.java, "weather_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWeatherCacheDao(db: WeatherDatabase): WeatherCacheDao = db.weatherCacheDao()

    @Provides
    fun provideSearchHistoryDao(db: WeatherDatabase): SearchHistoryDao = db.searchHistoryDao()
}
