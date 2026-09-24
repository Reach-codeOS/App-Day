package com.example.planner.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planner.data.local.DayEvent
import com.example.planner.data.prefs.SettingsManager
import com.example.planner.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class WeekState(
    val weekOffset: Int = 0,
    val selectedDay: Int = todayIso(),
    val events: List<DayEvent> = emptyList()
) {
    companion object {
        fun todayIso(): Int {
            val d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return if (d == 1) 7 else d - 1
        }
    }
}

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val repo: EventRepository,
    private val settings: SettingsManager
) : ViewModel() {

    private val _week = MutableStateFlow(0)
    private val _day = MutableStateFlow(WeekState.todayIso())

    val state: StateFlow<WeekState> = combine(_week, _day) { w, d -> w to d }
        .flatMapLatest { (w, d) ->
            repo.eventsForDay(w, d).map { list ->
                WeekState(weekOffset = w, selectedDay = d, events = list)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekState())

    fun setWeek(w: Int) {
        _week.value = w
        viewModelScope.launch {
            if (w == 1) repo.generateNextWeekFromRepeating()
            else repo.resetOriginalsForWeek(0)
        }
    }

    fun setDay(d: Int) { _day.value = d }

    fun toggleComplete(e: DayEvent) {
        viewModelScope.launch {
            if (e.isCompleted) {
                repo.update(e.copy(
                    isCompleted = false,
                    startHour = e.originalStartHour,
                    startMinute = e.originalStartMinute,
                    endHour = e.originalEndHour,
                    endMinute = e.originalEndMinute
                ))
            } else {
                val now = Calendar.getInstance()
                val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                repo.markCompleted(e.id, minutes)
            }
        }
    }

    fun delete(e: DayEvent) {
        viewModelScope.launch {
            if (e.isRepeating && e.weekOffset == 0) repo.deleteWithChildren(e.id)
            else repo.delete(e)
        }
    }
}
