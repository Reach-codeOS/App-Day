package com.example.planner.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.planner.data.local.DayEventDao
import com.example.planner.data.prefs.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class EventWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val dao: DayEventDao,
    private val settings: SettingsManager,
    private val notif: NotificationHelper
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getLong("eventId", -1L)
        val type = inputData.getString("type") ?: return Result.failure()
        val enabled = settings.areNotificationsEnabled.first()
        val sound = settings.isSoundEnabled.first()
        if (!enabled) return Result.success()

        val event = dao.getById(eventId) ?: return Result.success()
        when (type) {
            NotificationHelper.ACTION_START -> notif.showStart(event.title, sound)
            NotificationHelper.ACTION_END -> {
                // найдём следующее событие этого дня
                val all = dao.getEventsForDaySnapshot(event.weekOffset, event.dayOfWeek)
                val next = all.firstOrNull { it.startMinutes() > event.endMinutes() && !it.isCompleted }
                val nextInfo = next?.let {
                    "${it.title} в ${it.startHour.toString().padStart(2,'0')}:${it.startMinute.toString().padStart(2,'0')}"
                }
                notif.showEnd(event.title, nextInfo, sound)
            }
        }
        return Result.success()
    }
}

object Scheduler {
    fun scheduleEvent(ctx: Context, eventId: Long, weekOffset: Int, dayOfWeek: Int,
                      startH: Int, startM: Int, endH: Int, endM: Int, reminderMin: Int) {
        val wm = WorkManager.getInstance(ctx)

        // Убираем старые работы для этого события
        wm.cancelAllWorkByTag("event_$eventId")

        val startCal = calendarFor(weekOffset, dayOfWeek, startH, startM)
        val endCal = calendarFor(weekOffset, dayOfWeek, endH, endM)
        val reminderCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, -reminderMin)
        }
        val now = System.currentTimeMillis()

        if (reminderCal.timeInMillis > now) {
            enqueue(ctx, eventId, "start", reminderCal.timeInMillis - now)
        }
        if (endCal.timeInMillis > now) {
            enqueue(ctx, eventId, "end", endCal.timeInMillis - now)
        }
    }

    private fun enqueue(ctx: Context, eventId: Long, type: String, delay: Long) {
        val data = Data.Builder()
            .putLong("eventId", eventId)
            .putString("type", if (type == "start") NotificationHelper.ACTION_START else NotificationHelper.ACTION_END)
            .build()
        val req = OneTimeWorkRequestBuilder<EventWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("event_$eventId")
            .setInputData(data)
            .build()
        WorkManager.getInstance(ctx).enqueue(req)
    }

    private fun calendarFor(weekOffset: Int, dayOfWeek: Int, h: Int, m: Int): Calendar {
        val cal = Calendar.getInstance()
        val todayDow = cal.get(Calendar.DAY_OF_WEEK) // 1=Вс ... 7=Сб
        val isoToday = if (todayDow == 1) 7 else todayDow - 1 // 1=Пн ... 7=Вс
        val diff = dayOfWeek - isoToday + weekOffset * 7
        cal.add(Calendar.DAY_OF_YEAR, diff)
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
}