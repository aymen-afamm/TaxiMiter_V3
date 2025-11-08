package com.taximeter.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_stats")
data class DriverStatsEntity(
    @PrimaryKey val driverId: String,
    val totalTrips: Int,
    val totalRevenue: Double,
    val totalDistanceKm: Double
)