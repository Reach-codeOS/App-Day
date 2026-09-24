package com.example.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.planner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResource(R.string.sound_enabled), modifier = Modifier.weight(1f))
                Switch(checked = s.soundEnabled, onCheckedChange = { vm.setSound(it) })
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResource(R.string.notifications_enabled), modifier = Modifier.weight(1f))
                Switch(checked = s.notificationsEnabled, onCheckedChange = { vm.setNotifications(it) })
            }

            Text(stringResource(R.string.reminder_before))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { m ->
                    FilterChip(
                        selected = s.reminderMin == m,
                        onClick = { vm.setReminder(m) },
                        label = { Text("$m ${stringResource(R.string.minutes)}") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.clear_all))
            }

            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.version, "1.0"), style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_confirm_title)) },
            text = { Text(stringResource(R.string.clear_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { vm.clearAll(); showClearDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}