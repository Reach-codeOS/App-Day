package com.example.planner.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.planner.data.local.AppDatabase
import com.example.planner.data.prefs.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context, AppDatabase::class.java, "planner.db"
                ).build()
                val settings = SettingsManager(context)
                if (!settings.areNotificationsEnabled.first()) { pending.finish(); return@launch }
                val reminder = settings.reminderMinutesFlow.first()
                val all = db.dayEventDao().getAllForWeekSnapshot(0) +
                          db.dayEventDao().getAllForWeekSnapshot(1)
                for (e in all) {
                    if (e.isCompleted) continue
                    Scheduler.scheduleEvent(context, e.id, e.weekOffset, e.dayOfWeek,
                        e.startHour, e.startMinute, e.endHour, e.endMinute, reminder)
                }
            } finally { pending.finish() }
        }
    }
}