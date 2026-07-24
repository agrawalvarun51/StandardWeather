package com.example.standardweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.standardweather.data.local.dao.SearchHistoryDao
import com.example.standardweather.data.local.dao.WeatherCacheDao
import com.example.standardweather.data.local.entity.SearchHistoryEntity
import com.example.standardweather.data.local.entity.WeatherCacheEntity

@Database(
    entities = [WeatherCacheEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
