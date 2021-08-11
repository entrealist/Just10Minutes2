package com.codinginflow.just10minutes2.timer

import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TICK_DELAY = 500L

@Singleton
class TaskTimerManager @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val timerPreferencesManager: TimerPreferencesManager,
    private val taskDao: TaskDao
) {
    private val timerPreferencesFlow = timerPreferencesManager.timerPreferencesFlow

    val activeTask = timerPreferencesFlow.flatMapLatest { timerPreferences ->
        timerPreferences.activeTaskId?.let { taskId ->
            taskDao.getTaskById(taskId)
        } ?: emptyFlow()
    }

    private val runningFlow = MutableStateFlow(false)
    val running: Flow<Boolean> = runningFlow

    var timerJob: Job? = null

    init {
        applicationScope.launch {
            activeTask.collect { task ->
                if (task != null && task.timeLeftTodayInMilliseconds <= 0) stopTimer()
            }
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = applicationScope.launch {
            val activeTask = activeTask.first()
            if (activeTask != null) {
                runningFlow.value = true
                while (true) {
                    delay(TICK_DELAY)
                    taskDao.increaseMillisCompletedToday(activeTask.id, TICK_DELAY)
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        runningFlow.value = false
    }

    // TODO: 12.08.2021 Handle process death
}
