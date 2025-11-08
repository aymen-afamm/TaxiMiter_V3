package com.taximeter.app.models

import java.util.Date

data class Trip(
    val id: String,
    val driverId: String,
    val clientId: String? = null,
    val startTime: Date,
    val endTime: Date? = null,
    val startLocation: TripLocation,
    val endLocation: TripLocation? = null,
    val distanceKm: Double = 0.0,
    val durationMinutes: Double = 0.0,
    val baseFare: Double = 2.5,
    val farePerKm: Double = 1.5,
    val farePerMinute: Double = 0.5,
    val extraCharges: Double = 0.0,
    val discount: Double = 0.0,
    val totalFare: Double = 0.0,
    val status: TripStatus = TripStatus.PENDING,
    val rating: Float? = null
)

data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

enum class TripStatus {
    PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELED
}
