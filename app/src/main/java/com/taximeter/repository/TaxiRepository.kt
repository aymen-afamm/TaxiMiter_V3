package com.taximeter.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.taximeter.app.database.TaxiDatabase
import com.taximeter.app.models.*
import kotlinx.coroutines.flow.Flow

class TaxiRepository(private val context: Context) {
    private val database = TaxiDatabase.getDatabase(context)
    private val tripDao = database.tripDao()
    private val statsDao = database.driverStatsDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("taxi_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Authentication
    fun login(username: String, password: String): User? {
        val users = getPreDefinedUsers()
        return users.find { it.username == username && it.password == password }
    }

    fun saveCurrentUser(user: User) {
        prefs.edit().putString("current_user", gson.toJson(user)).apply()
    }

    fun getCurrentUser(): User? {
        val json = prefs.getString("current_user", null)
        return json?.let { gson.fromJson(it, User::class.java) }
    }

    fun logout() {
        prefs.edit().remove("current_user").apply()
    }

    // Predefined users for local authentication
    private fun getPreDefinedUsers(): List<User> {
        return listOf(
            User(
                id = "driver1",
                username = "karima",
                password = "1234",
                userType = UserType.DRIVER,
                profile = UserProfile(
                    name = "ALAMI",
                    firstName = "Karima",
                    age = 35,
                    licenseType = "Permis A",
                    phone = "+212 6 12 34 56 78"
                )
            ),
            User(
                id = "driver2",
                username = "ahmed",
                password = "1234",
                userType = UserType.DRIVER,
                profile = UserProfile(
                    name = "BENANI",
                    firstName = "Ahmed",
                    age = 42,
                    licenseType = "Permis B",
                    phone = "+212 6 98 76 54 32"
                )
            ),
            User(
                id = "client1",
                username = "sara",
                password = "1234",
                userType = UserType.CLIENT,
                profile = UserProfile(
                    name = "IDRISSI",
                    firstName = "Sara",
                    age = 28,
                    phone = "+212 6 11 22 33 44"
                )
            )
        )
    }

    // Settings
    fun saveFareSettings(baseFare: Double, farePerKm: Double, farePerMinute: Double) {
        prefs.edit().apply {
            putFloat("base_fare", baseFare.toFloat())
            putFloat("fare_per_km", farePerKm.toFloat())
            putFloat("fare_per_minute", farePerMinute.toFloat())
        }.apply()
    }

    fun getFareSettings(): Triple<Double, Double, Double> {
        return Triple(
            prefs.getFloat("base_fare", 2.5f).toDouble(),
            prefs.getFloat("fare_per_km", 1.5f).toDouble(),
            prefs.getFloat("fare_per_minute", 0.5f).toDouble()
        )
    }

    // Language settings
    fun saveLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("language", "fr") ?: "fr"
    }
}