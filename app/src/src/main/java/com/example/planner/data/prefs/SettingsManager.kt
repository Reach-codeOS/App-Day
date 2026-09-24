package com.example.planner.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    private val soundEnabled = booleanPreferencesKey("sound_enabled")
    private val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    private val reminderMinutes = intPreferencesKey("reminder_minutes")

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { it[soundEnabled] ?: true }
    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[notificationsEnabled] ?: true }
    val reminderMinutesFlow: Flow<Int> = context.dataStore.data.map { it[reminderMinutes] ?: 10 }

    suspend fun setSoundEnabled(v: Boolean) = context.dataStore.edit { it[soundEnabled] = v }
    suspend fun setNotificationsEnabled(v: Boolean) = context.dataStore.edit { it[notificationsEnabled] = v }
    suspend fun setReminderMinutes(v: Int) = context.dataStore.edit { it[reminderMinutes] = v }
}