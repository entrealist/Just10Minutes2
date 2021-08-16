package com.codinginflow.just10minutes2.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.containsDate
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TICK_DELAY = 500L

@Singleton
class TaskTimerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val timerPreferencesManager: TimerPreferencesManager,
    private val dayCheckPreferencesManager: DayCheckPreferencesManager,
    private val taskDao: TaskDao
) {
    private val timerPreferencesFlow = timerPreferencesManager.timerPreferencesFlow

    val activeTask = timerPreferencesFlow.flatMapLatest { timerPreferences ->
        timerPreferences.activeTaskId?.let { taskId ->
            taskDao.getNotArchivedTaskById(taskId)
        } ?: flowOf(null)
    }

    private val activeDay = dayCheckPreferencesManager.activeDay

    private val timerRunningFlow = MutableStateFlow(false)
    val timerRunning: Flow<Boolean> = timerRunningFlow

    private val timerFinishedChannel = Channel<Task>()
    val timerFinished = timerFinishedChannel.receiveAsFlow()

    var timerJob: Job? = null

    init {
        applicationScope.launch {
            activeTask.collect { task ->
                if (task != null) {
                    // Task has just finished
                    if (task.isCompletedToday && timerRunning.first()) {
                        timerFinishedChannel.send(task)
                        stopTimer()
                    }

                    // stop timer if active day not in task weekdays (e.g. after a Task update)
                    val activeDay = activeDay.first()
                    if (activeDay != null && !task.weekdays.containsDate(activeDay)) {
                        Timber.d("activeDay = $activeDay")
                        stopTimer()
                    }
                }
            }
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = applicationScope.launch {
            val activeTask = activeTask.first()
            if (activeTask != null) {
                startTimerService()
                timerRunningFlow.value = true
                while (true) {
                    delay(TICK_DELAY)
                    taskDao.increaseMillisCompletedToday(activeTask.id, TICK_DELAY)
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        stopTimerService()
        timerRunningFlow.value = false
    }

    suspend fun stopTimerIfTaskIsActive(task: Task) {
        val activeTask = activeTask.first()
        if (activeTask?.id == task.id) {
            stopTimer()
        }
    }

    private fun startTimerService() {
        val serviceIntent = Intent(context, TimerService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun stopTimerService() {
        val serviceIntent = Intent(context, TimerService::class.java)
        context.stopService(serviceIntent)
    }

    // TODO: 12.08.2021 Handle process death
}
