package com.codinginflow.just10minutes2.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.application.MainActivity
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.util.formatTimeText
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// TODO: 16.08.2021 Add Stop button to notification

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var notificationManager = NotificationManagerCompat.from(context)

    private val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    private val pendingIntentFlag =
        if (Build.VERSION.SDK_INT > 23) PendingIntent.FLAG_IMMUTABLE else 0

    private val openActivityPendingIntent: PendingIntent =
        PendingIntent.getActivity(context, 0, intent, pendingIntentFlag)

    init {
        createNotificationChannels()
    }

    fun showNewDayNotification() {
        val notification = NotificationCompat.Builder(context, NEW_DAY_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sun)
            .setColor(ContextCompat.getColor(context, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setContentTitle(context.getString(R.string.new_day_has_started))
            .setContentText(context.getString(R.string.new_day_started_message))
            .setAutoCancel(true)
        notificationManager.notify(NEW_DAY_NOTIFICATION_ID, notification.build())
    }

    fun getEmptyTimerServiceNotification() =
        NotificationCompat.Builder(context, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setColor(ContextCompat.getColor(context, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setSilent(true)

    fun updateTimerServiceNotification(task: Task) {
        val notification = NotificationCompat.Builder(context, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setColor(ContextCompat.getColor(context, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setSilent(true)
            .setContentTitle(task.name)
            .setContentText(formatTimeText(task.timeLeftTodayInMilliseconds))
        notificationManager.notify(TIMER_NOTIFICATION_ID, notification.build())
    }

    fun showTaskFinishedNotification(task: Task) {
        val finishNotification = NotificationCompat.Builder(context, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(context.getString(R.string.task_completed_exclamation_mark))
            .setContentText(task.name)
            .setColor(ContextCompat.getColor(context, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(task.id.toInt(), finishNotification)
    }

    fun cancelTimerServiceNotification() {
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerServiceChannel = NotificationChannel(
                TIMER_SERVICE_CHANNEL_ID,
                context.getString(R.string.timer_service_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            timerServiceChannel.description =
                context.getString(R.string.timer_service_notification_channel_description)
            notificationManager.createNotificationChannel(timerServiceChannel)

            val newDayNotificationChannel = NotificationChannel(
                NEW_DAY_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.new_day_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            newDayNotificationChannel.description =
                context.getString(R.string.new_day_notification_channel_description)
            notificationManager.createNotificationChannel(newDayNotificationChannel)
        }
    }

    companion object {
        const val TIMER_SERVICE_CHANNEL_ID = "TimerServiceChannel"
        const val NEW_DAY_NOTIFICATION_CHANNEL_ID = "NewDayNotificationChannel"
        const val TIMER_NOTIFICATION_ID = 123
        const val NEW_DAY_NOTIFICATION_ID = 123
    }
}