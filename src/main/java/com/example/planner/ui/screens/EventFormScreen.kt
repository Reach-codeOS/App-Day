package com.example.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.planner.R

private val PALETTE = listOf(
    0xFFE57373, 0xFFFFB74D, 0xFFFFF176, 0xFF81C784,
    0xFF4FC3F7, 0xFFBA68C8, 0xFFA1887F, 0xFF90A4AE
)
private val ICONS = listOf("default","work","sport","study","rest")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    weekOffset: Int,
    dayOfWeek: Int,
    eventId: Long?,
    onBack: () -> Unit,
    vm: EventFormViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(eventId) { vm.init(eventId, weekOffset, dayOfWeek) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) stringResource(R.string.new_event) else stringResource(R.string.edit_event)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { vm.setTitle(it) },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeField(
                    label = stringResource(R.string.start),
                    hour = state.startH, minute = state.startM,
                    onPick = { h, m -> vm.setStart(h, m) },
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    label = stringResource(R.string.end),
                    hour = state.endH, minute = state.endM,
                    onPick = { h, m -> vm.setEnd(h, m) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { vm.setDescription(it) },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 6
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.isRepeating, onCheckedChange = { vm.setRepeating(it) })
                Text(stringResource(R.string.repeat_weekly))
            }

            Text(stringResource(R.string.color), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PALETTE.forEach { c ->
                    val sel = c == state.color
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c.toInt()))
                            .then(if (sel) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { vm.setColor(c.toInt()) }
                    )
                }
            }

            Text(stringResource(R.string.icon), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ICONS.forEach { ic ->
                    val icon = when (ic) {
                        "work" -> Icons.Default.Work
                        "sport" -> Icons.Default.FitnessCenter
                        "study" -> Icons.Default.School
                        "rest" -> Icons.Default.Bedtime
                        else -> Icons.Default.Event
                    }
                    val sel = ic == state.iconType
                    IconButton(
                        onClick = { vm.setIcon(ic) },
                        colors = IconButtonColors(
                            containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Gray
                        )
                    ) { Icon(icon, contentDescription = ic) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.save(onBack) },
                enabled = state.title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    label: String, hour: Int, minute: Int,
    onPick: (Int, Int) -> Unit, modifier: Modifier
) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = "${hour.toString().padStart(2,'0')}:${minute.toString().padStart(2,'0')}",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { show = true },
        trailingIcon = { Icon(Icons.Default.Schedule, null) }
    )
    if (show) {
        TimePickerDialog(
            onDismiss = { show = false },
            onConfirm = { h, m -> onPick(h, m); show = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.pick_time)) },
        text = { TimePicker(state = state) }
    )
}