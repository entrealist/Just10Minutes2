package com.codinginflow.just10minutes2.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.codinginflow.just10minutes2.MainActivity
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.util.formatTimeText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var taskTimerManager: TaskTimerManager

    private val serviceScope = CoroutineScope(SupervisorJob())

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var notification: NotificationCompat.Builder

    private lateinit var openActivityPendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlag = if (Build.VERSION.SDK_INT > 23) PendingIntent.FLAG_IMMUTABLE else 0
        openActivityPendingIntent =
            PendingIntent.getActivity(this, 0, intent, pendingIntentFlag)

        notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setColor(ContextCompat.getColor(this, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setSilent(true)

        startForeground(TIMER_NOTIFICATION_ID, notification.build())

        serviceScope.launch {
            taskTimerManager.activeTask.collect { task ->
                if (task != null) {
                    updateNotification(task)
                }
            }
        }

        serviceScope.launch {
            taskTimerManager.timerFinished.collect { task ->
                onTaskFinished(task)
            }
        }
    }

    private fun updateNotification(task: Task) {
        notification
            .setContentTitle(task.name)
            .setContentText(formatTimeText(task.timeLeftTodayInMilliseconds))
        notificationManager.notify(TIMER_NOTIFICATION_ID, notification.build())
    }

    private fun onTaskFinished(task: Task) {
        val finishNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getString(R.string.task_completed_exclamation_mark))
            .setContentText(task.name)
            .setColor(ContextCompat.getColor(this, R.color.design_default_color_primary))
            .setContentIntent(openActivityPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(task.id.toInt(), finishNotification)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.timer_service_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = getString(R.string.timer_service_notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }

    override fun onBind(p0: Intent?): IBinder? = null
}

private const val NOTIFICATION_CHANNEL_ID = "TimerServiceChannel"
private const val TIMER_NOTIFICATION_ID = 123