package com.taximeter.app.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverStatsDao {
    @Query("SELECT * FROM driver_stats WHERE driverId = :driverId")
    fun getDriverStats(driverId: String): Flow<DriverStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DriverStatsEntity)

    @Update
    suspend fun updateStats(stats: DriverStatsEntity)
}