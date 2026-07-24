package com.example.standardweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.standardweather.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherCacheEntity)

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    fun observeWeather(cityId: String): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    suspend fun getWeather(cityId: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache ORDER BY fetchedAt DESC")
    fun observeAllCached(): Flow<List<WeatherCacheEntity>>

    @Query("DELETE FROM weather_cache WHERE cityId = :cityId")
    suspend fun delete(cityId: String)

    @Query("SELECT fetchedAt FROM weather_cache WHERE cityId = :cityId")
    suspend fun getFetchedAt(cityId: String): Long?
}
