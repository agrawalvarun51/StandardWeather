package com.example.standardweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.standardweather.data.local.dao.SearchHistoryDao
import com.example.standardweather.data.local.dao.WeatherCacheDao
import com.example.standardweather.data.local.entity.SearchHistoryEntity
import com.example.standardweather.data.local.entity.WeatherCacheEntity

@Database(
    entities = [WeatherCacheEntity::class, SearchHistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
