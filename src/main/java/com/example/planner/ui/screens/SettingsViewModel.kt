package com.example.planner.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planner.data.prefs.SettingsManager
import com.example.planner.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val reminderMin: Int = 10
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsManager,
    private val repo: EventRepository
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        settings.isSoundEnabled,
        settings.areNotificationsEnabled,
        settings.reminderMinutesFlow
    ) { s, n, r -> SettingsState(s, n, r) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setSound(v: Boolean) = viewModelScope.launch { settings.setSoundEnabled(v) }
    fun setNotifications(v: Boolean) = viewModelScope.launch { settings.setNotificationsEnabled(v) }
    fun setReminder(v: Int) = viewModelScope.launch { settings.setReminderMinutes(v) }
    fun clearAll() = viewModelScope.launch { repo.deleteAll() }
}