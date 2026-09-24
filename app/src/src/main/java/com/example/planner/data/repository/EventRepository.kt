package com.example.planner.data.repository

import com.example.planner.data.local.DayEvent
import com.example.planner.data.local.DayEventDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class EventRepository(private val dao: DayEventDao) {

    fun eventsForDay(weekOffset: Int, dayOfWeek: Int): Flow<List<DayEvent>> =
        dao.getEventsForDay(weekOffset, dayOfWeek)

    fun allForWeek(weekOffset: Int): Flow<List<DayEvent>> =
        dao.getAllForWeek(weekOffset)

    suspend fun getById(id: Long): DayEvent? = dao.getById(id)

    suspend fun insert(event: DayEvent): Long = dao.insert(event)
    suspend fun update(event: DayEvent) = dao.update(event)
    suspend fun delete(event: DayEvent) = dao.delete(event)
    suspend fun deleteWithChildren(id: Long) = dao.deleteWithChildren(id)
    suspend fun deleteAll() = dao.deleteAll()

    /**
     * КРИТИЧНАЯ ЛОГИКА: при завершении события ДО его окончания
     * сдвигаем все последующие события дня на (scheduledEnd - actualEnd) минут назад.
     */
    suspend fun markCompleted(eventId: Long, actualCompletionMinutes: Int) {
        val event = dao.getById(eventId) ?: return
        if (event.isCompleted) return

        val scheduledEnd = event.endMinutes()
        val remaining = scheduledEnd - actualCompletionMinutes
        if (remaining <= 0) {
            // Завершено после или точно в срок — просто помечаем
            dao.update(event.copy(isCompleted = true))
            return
        }

        // Обновляем само событие: сдвигаем конец к моменту завершения
        val newEndHour = actualCompletionMinutes / 60
        val newEndMinute = actualCompletionMinutes % 60
        dao.update(event.copy(
            isCompleted = true,
            endHour = newEndHour,
            endMinute = newEndMinute
        ))

        // Сдвигаем все последующие события того же дня и недели
        val subsequent = dao.getEventsForDay(event.weekOffset, event.dayOfWeek)
            // Flow -> нужно получить snapshot через suspend-запрос
            .let { flow -> mutableListOf<DayEvent>().also { /* placeholder */ } }

        // Получим события синхронно через отдельный запрос
        val allDay = dao.getEventsForDaySnapshot(event.weekOffset, event.dayOfWeek)
        for (e in allDay) {
            if (e.id == eventId) continue
            if (e.startMinutes() <= event.endMinutes()) continue // не раньше конца завершённого
            if (e.isCompleted) continue

            val newStart = (e.startMinutes() - remaining).coerceAtLeast(0)
            val newEnd = (e.endMinutes() - remaining).coerceAtLeast(newStart)
            dao.update(e.copy(
                startHour = newStart / 60,
                startMinute = newStart % 60,
                endHour = newEnd / 60,
                endMinute = newEnd % 60
            ))
        }
    }

    /**
     * При переходе на новую неделю — копируем повторяющиеся события из текущей (weekOffset=0)
     * в следующую (weekOffset=1), сохраняя оригинальные времена.
     */
    suspend fun generateNextWeekFromRepeating() {
        for (day in 1..7) {
            val bases = dao.getBaseRepeating(day)
            for (base in bases) {
                val copy = base.copy(
                    id = 0,
                    weekOffset = 1,
                    parentId = base.id,
                    startHour = base.originalStartHour,
                    startMinute = base.originalStartMinute,
                    endHour = base.originalEndHour,
                    endMinute = base.originalEndMinute,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )
                dao.insert(copy)
            }
        }
    }

    /**
     * При возврате к "текущей" неделе — обновляем original* к текущим значениям.
     */
    suspend fun resetOriginalsForWeek(weekOffset: Int) {
        val all = dao.getAllForWeekSnapshot(weekOffset)
        for (e in all) {
            dao.update(e.copy(
                originalStartHour = e.startHour,
                originalStartMinute = e.startMinute,
                originalEndHour = e.endHour,
                originalEndMinute = e.endMinute
            ))
        }
    }
}