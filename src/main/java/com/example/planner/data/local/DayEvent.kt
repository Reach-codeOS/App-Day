package com.example.planner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class DayEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dayOfWeek: Int,          // 1=Пн ... 7=Вс
    val weekOffset: Int,         // 0=текущая, 1=следующая
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isCompleted: Boolean = false,
    val isRepeating: Boolean = false,
    val color: Int,              // ARGB
    val iconType: String = "default",
    val createdAt: Long = System.currentTimeMillis(),
    val parentId: Long? = null,
    val originalStartHour: Int = startHour,
    val originalStartMinute: Int = startMinute,
    val originalEndHour: Int = endHour,
    val originalEndMinute: Int = endMinute
) {
    fun startMinutes(): Int = startHour * 60 + startMinute
    fun endMinutes(): Int = endHour * 60 + endMinute
    fun originalStartMinutes(): Int = originalStartHour * 60 + originalStartMinute
    fun originalEndMinutes(): Int = originalEndHour * 60 + originalEndMinute
}