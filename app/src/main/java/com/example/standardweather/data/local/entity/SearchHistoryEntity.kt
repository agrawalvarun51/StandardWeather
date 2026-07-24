package com.example.standardweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists user's recently searched cities for quick re-access.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val cityId: String,
    val cityName: String,
    val country: String,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val searchedAt: Long = System.currentTimeMillis()
)
