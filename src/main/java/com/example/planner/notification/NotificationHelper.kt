package com.example.planner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.planner.MainActivity
import com.example.planner.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "schedule_channel"
        const val ACTION_START = "action_event_start"
        const val ACTION_END = "action_event_end"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NEXT = "extra_next"
    }

    init { createChannel() }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Schedule",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Event start/end notifications"
            enableVibration(true)
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    fun showStart(title: String, playSound: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notif_start))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pi)
            .setAutoCancel(true)
        if (playSound) n.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        getNm().notify(title.hashCode(), n.build())
    }

    fun showEnd(title: String, nextInfo: String?, playSound: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val text = buildString {
            append(context.getString(R.string.notif_end, title))
            if (!nextInfo.isNullOrBlank()) {
                append("\n").append(context.getString(R.string.notif_next, nextInfo))
            }
        }
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notif_end_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
        if (playSound) n.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        getNm().notify((title.hashCode() + 1), n.build())
    }

    private fun getNm() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}