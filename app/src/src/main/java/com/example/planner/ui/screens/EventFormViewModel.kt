package com.example.planner.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planner.data.local.DayEvent
import com.example.planner.data.prefs.SettingsManager
import com.example.planner.data.repository.EventRepository
import com.example.planner.notification.Scheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventFormState(
    val id: Long? = null,
    val weekOffset: Int = 0,
    val dayOfWeek: Int = 1,
    val title: String = "",
    val description: String = "",
    val startH: Int = 9, val startM: Int = 0,
    val endH: Int = 10, val endM: Int = 0,
    val isRepeating: Boolean = false,
    val color: Int = 0xFF4FC3F7.toInt(),
    val iconType: String = "default"
)

@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val repo: EventRepository,
    private val settings: SettingsManager
) : ViewModel() {

    private val _state = MutableStateFlow(EventFormState())
    val state: StateFlow<EventFormState> = _state

    fun init(eventId: Long?, weekOffset: Int, dayOfWeek: Int) {
        _state.value = _state.value.copy(weekOffset = weekOffset, dayOfWeek = dayOfWeek)
        if (eventId != null) {
            viewModelScope.launch {
                val e = repo.getById(eventId) ?: return@launch
                _state.value = EventFormState(
                    id = e.id, weekOffset = e.weekOffset, dayOfWeek = e.dayOfWeek,
                    title = e.title, description = e.description,
                    startH = e.startHour, startM = e.startMinute,
                    endH = e.endHour, endM = e.endMinute,
                    isRepeating = e.isRepeating, color = e.color, iconType = e.iconType
                )
            }
        }
    }

    fun setTitle(v: String) { _state.value = _state.value.copy(title = v) }
    fun setDescription(v: String) { _state.value = _state.value.copy(description = v) }
    fun setStart(h: Int, m: Int) { _state.value = _state.value.copy(startH = h, startM = m) }
    fun setEnd(h: Int, m: Int) { _state.value = _state.value.copy(endH = h, endM = m) }
    fun setRepeating(v: Boolean) { _state.value = _state.value.copy(isRepeating = v) }
    fun setColor(v: Int) { _state.value = _state.value.copy(color = v) }
    fun setIcon(v: String) { _state.value = _state.value.copy(iconType = v) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            val entity = DayEvent(
                id = s.id ?: 0,
                title = s.title,
                description = s.description,
                dayOfWeek = s.dayOfWeek,
                weekOffset = s.weekOffset,
                startHour = s.startH, startMinute = s.startM,
                endHour = s.endH, endMinute = s.endM,
                isRepeating = s.isRepeating,
                color = s.color,
                iconType = s.iconType,
                originalStartHour = s.startH, originalStartMinute = s.startM,
                originalEndHour = s.endH, originalEndMinute = s.endM
            )
            val savedId = if (s.id == null) repo.insert(entity) else { repo.update(entity); s.id!! }
            // планируем уведомление (нужен Context — передаём через AndroidViewModel в финальной версии)
            onDone()
        }
    }
}