package com.example.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.planner.R
import com.example.planner.ui.components.EventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    onAddEvent: (Int, Int) -> Unit,
    onEditEvent: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    vm: WeekViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.week_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddEvent(state.weekOffset, state.selectedDay) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Переключатель недели
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.weekOffset == 0,
                    onClick = { vm.setWeek(0) },
                    label = { Text(stringResource(R.string.this_week)) }
                )
                FilterChip(
                    selected = state.weekOffset == 1,
                    onClick = { vm.setWeek(1) },
                    label = { Text(stringResource(R.string.next_week)) }
                )
            }

            // Дни недели
            ScrollableTabRow(
                selectedTabIndex = (state.selectedDay - 1).coerceIn(0, 6),
                edgePadding = 8.dp
            ) {
                val days = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")
                days.forEachIndexed { i, d ->
                    Tab(
                        selected = state.selectedDay == (i + 1),
                        onClick = { vm.setDay(i + 1) },
                        text = { Text(d) }
                    )
                }
            }

            // Список событий
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.events, key = { it.id }) { ev ->
                    EventCard(
                        event = ev,
                        onToggleComplete = { vm.toggleComplete(ev) },
                        onDelete = { vm.delete(ev) },
                        onEdit = { onEditEvent(ev.id) }
                    )
                }
            }
        }
    }
}