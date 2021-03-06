package com.codinginflow.just10minutes2.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.notification.NotificationHelper
import com.codinginflow.just10minutes2.notification.NotificationHelper.Companion.TIMER_NOTIFICATION_ID
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

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        startForeground(TIMER_NOTIFICATION_ID, notificationHelper.getEmptyTimerServiceNotification().build())

        serviceScope.launch {
            taskTimerManager.selectedTask.collect { task ->
                if (task != null) {
                    notificationHelper.updateTimerServiceNotification(task)
                }
            }
        }

        serviceScope.launch {
            taskTimerManager.timerFinished.collect { task ->
                onTaskFinished(task)
            }
        }
    }

    private fun onTaskFinished(task: Task) {
        notificationHelper.showTaskFinishedNotification(task)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationHelper.cancelTimerServiceNotification()
    }

    override fun onBind(p0: Intent?): IBinder? = null
}