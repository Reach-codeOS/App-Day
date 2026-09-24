package com.example.planner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DayEvent::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dayEventDao(): DayEventDao
}