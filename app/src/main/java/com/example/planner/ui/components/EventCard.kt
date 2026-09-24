package com.example.planner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.planner.data.local.DayEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(event: DayEvent, onToggleComplete: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    val swiped = offsetX > 200f
    LaunchedEffect(swiped) { if (swiped) onDelete() }

    Card(modifier = Modifier.fillMaxWidth()
        .pointerInput(event.id) { detectHorizontalDragGestures(onDragEnd = { offsetX = 0f }, onDragCancel = { offsetX = 0f }) { _, delta -> offsetX = (offsetX + delta).coerceAtLeast(0f) } }
        .pointerInput(event.id) { detectTapGestures(onLongPress = { onEdit() }) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = event.isCompleted, onCheckedChange = { onToggleComplete() })
            Box(Modifier.width(6.dp).height(64.dp).background(Color(event.color)))
            Column(Modifier.padding(12.dp).weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${fmt(event.startHour)}:${fmt(event.startMinute)} – ${fmt(event.endHour)}:${fmt(event.endMinute)}", style = MaterialTheme.typography.bodySmall)
                if (event.description.isNotBlank()) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Описание")
                    }
                    AnimatedVisibility(expanded) { Text(event.description, style = MaterialTheme.typography.bodyMedium) }
                }
            }
            if (event.iconType != "default") {
                Icon(categoryIcon(event.iconType), contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            }
            IconButton(onClick = { onDelete() }, modifier = Modifier.padding(end = 4.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
private fun fmt(v: Int) = v.toString().padStart(2, '0')
private fun categoryIcon(t: String) = when (t) {
    "work" -> Icons.Default.Work; "sport" -> Icons.Default.FitnessCenter; "study" -> Icons.Default.School; "rest" -> Icons.Default.Bedtime; else -> Icons.Default.Event
}
