package com.taximeter.app.viewmodels

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taximeter.app.repository.TaxiRepository
import java.util.*

class TaxiMeterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaxiRepository(application)

    private val _tripState = MutableLiveData<TripState>()
    val tripState: LiveData<TripState> = _tripState

    private val _distanceKm = MutableLiveData<Double>(0.0)
    val distanceKm: LiveData<Double> = _distanceKm

    private val _durationMinutes = MutableLiveData<Double>(0.0)
    val durationMinutes: LiveData<Double> = _durationMinutes

    private val _totalFare = MutableLiveData<Double>(0.0)
    val totalFare: LiveData<Double> = _totalFare

    private var startTime: Date? = null
    private var lastLocation: Location? = null
    private var accumulatedDistance: Double = 0.0
    private var isPaused = false

    // Fare settings
    private var baseFare = 2.5
    private var farePerKm = 1.5
    private var farePerMinute = 0.5

    init {
        loadFareSettings()
        _tripState.value = TripState.IDLE
    }

    private fun loadFareSettings() {
        val settings = repository.getFareSettings()
        baseFare = settings.first
        farePerKm = settings.second
        farePerMinute = settings.third
    }

    fun startTrip() {
        startTime = Date()
        lastLocation = null
        accumulatedDistance = 0.0
        isPaused = false
        _distanceKm.value = 0.0
        _durationMinutes.value = 0.0
        _totalFare.value = baseFare
        _tripState.value = TripState.RUNNING
    }

    fun pauseTrip() {
        isPaused = true
        _tripState.value = TripState.PAUSED
    }

    fun resumeTrip() {
        isPaused = false
        _tripState.value = TripState.RUNNING
    }

    fun endTrip() {
        _tripState.value = TripState.COMPLETED
    }

    fun resetTrip() {
        startTime = null
        lastLocation = null
        accumulatedDistance = 0.0
        isPaused = false
        _distanceKm.value = 0.0
        _durationMinutes.value = 0.0
        _totalFare.value = 0.0
        _tripState.value = TripState.IDLE
    }

    fun updateLocation(location: Location) {
        if (isPaused || _tripState.value != TripState.RUNNING) return

        // Calculate distance
        lastLocation?.let { last ->
            val distance = last.distanceTo(location) / 1000.0 // Convert to km
            if (distance > 0.001) { // Filter noise
                accumulatedDistance += distance
                _distanceKm.value = accumulatedDistance
            }
        }
        lastLocation = location

        // Calculate duration
        startTime?.let { start ->
            val duration = (Date().time - start.time) / 60000.0 // Convert to minutes
            _durationMinutes.value = duration
        }

        // Calculate fare
        calculateFare()
    }

    private fun calculateFare() {
        val distance = _distanceKm.value ?: 0.0
        val duration = _durationMinutes.value ?: 0.0
        val fare = baseFare + (distance * farePerKm) + (duration * farePerMinute)
        _totalFare.value = fare
    }

    enum class TripState {
        IDLE, RUNNING, PAUSED, COMPLETED
    }
}