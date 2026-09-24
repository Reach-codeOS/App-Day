package com.example.planner.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEventDao {
    @Query("SELECT * FROM events WHERE weekOffset = :weekOffset AND dayOfWeek = :day ORDER BY startHour, startMinute")
    fun getEventsForDay(weekOffset: Int, dayOfWeek: Int): Flow<List<DayEvent>>

    @Query("SELECT * FROM events WHERE weekOffset = :weekOffset ORDER BY dayOfWeek, startHour, startMinute")
    fun getAllForWeek(weekOffset: Int): Flow<List<DayEvent>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): DayEvent?

    @Insert
    suspend fun insert(event: DayEvent): Long

    @Update
    suspend fun update(event: DayEvent)

    @Delete
    suspend fun delete(event: DayEvent)

    @Query("DELETE FROM events WHERE id = :id OR parentId = :id")
    suspend fun deleteWithChildren(id: Long)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("SELECT * FROM events WHERE weekOffset = :weekOffset AND dayOfWeek = :day AND isRepeating = 1")
    suspend fun getRepeatingForDay(weekOffset: Int, dayOfWeek: Int): List<DayEvent>

    @Query("SELECT * FROM events WHERE dayOfWeek = :day AND isRepeating = 1 AND weekOffset = 0")
    suspend fun getBaseRepeating(dayOfWeek: Int): List<DayEvent>
}