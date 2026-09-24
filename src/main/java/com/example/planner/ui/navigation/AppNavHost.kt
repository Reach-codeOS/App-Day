package com.example.planner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.planner.ui.screens.EventFormScreen
import com.example.planner.ui.screens.SettingsScreen
import com.example.planner.ui.screens.WeekScreen

object Routes {
    const val WEEK = "week"
    const val SETTINGS = "settings"
    const val EVENT_NEW = "event/new/{weekOffset}/{dayOfWeek}"
    const val EVENT_EDIT = "event/edit/{eventId}"

    fun newEvent(weekOffset: Int, day: Int) = "event/new/$weekOffset/$day"
    fun editEvent(id: Long) = "event/edit/$id"
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.WEEK) {
        composable(Routes.WEEK) {
            WeekScreen(
                onAddEvent = { w, d -> nav.navigate(Routes.newEvent(w, d)) },
                onEditEvent = { id -> nav.navigate(Routes.editEvent(id)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.EVENT_NEW) { back ->
            val w = back.arguments?.getString("weekOffset")?.toIntOrNull() ?: 0
            val d = back.arguments?.getString("dayOfWeek")?.toIntOrNull() ?: 1
            EventFormScreen(weekOffset = w, dayOfWeek = d, eventId = null, onBack = { nav.popBackStack() })
        }
        composable(Routes.EVENT_EDIT) { back ->
            val id = back.arguments?.getString("eventId")?.toLongOrNull() ?: return@composable
            EventFormScreen(weekOffset = 0, dayOfWeek = 1, eventId = id, onBack = { nav.popBackStack() })
        }
    }
}