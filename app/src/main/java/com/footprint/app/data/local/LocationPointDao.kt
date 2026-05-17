package com.footprint.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationPointDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationPoint(locationPoint: LocationPoint): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationPoints(locationPoints: List<LocationPoint>): List<Long>

    @Query(
        """
        SELECT *
        FROM location_points
        WHERE recordedAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
        ORDER BY recordedAtEpochMillis ASC, id ASC
        """
    )
    suspend fun getPointsBetween(
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<LocationPoint>

    @Query(
        """
        SELECT *
        FROM location_points
        ORDER BY recordedAtEpochMillis ASC, id ASC
        """
    )
    suspend fun getAllPointsOrderedByTime(): List<LocationPoint>

    @Query("DELETE FROM location_points WHERE recordedAtEpochMillis < :olderThanEpochMillis")
    suspend fun deletePointsOlderThan(olderThanEpochMillis: Long): Int

    @Query(
        """
        DELETE FROM location_points
        WHERE recordedAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
        """
    )
    suspend fun deletePointsBetween(
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Int

    @Query("DELETE FROM location_points")
    suspend fun deleteAllPoints(): Int
}
