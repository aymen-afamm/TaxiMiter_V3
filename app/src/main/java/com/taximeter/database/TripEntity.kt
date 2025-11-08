package com.taximeter.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val clientId: String?,
    val startTime: Date,
    val endTime: Date?,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double?,
    val endLng: Double?,
    val distanceKm: Double,
    val durationMinutes: Double,
    val totalFare: Double,
    val status: String
)