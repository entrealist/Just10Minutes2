package com.codinginflow.just10minutes2.timer

import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TICK_DELAY = 500L

@Singleton
class TaskTimerManager @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val taskDao: TaskDao
) {
    private val runningFlow = MutableStateFlow(false)
    val running: Flow<Boolean> = runningFlow

    var timerJob: Job? = null

    fun startTimerForTask(taskId: Long) {
        timerJob?.cancel()
        runningFlow.value = true
        timerJob = applicationScope.launch {
            while (true) {
                delay(TICK_DELAY)
                increaseMillisCompletedTodayForTask(taskId, TICK_DELAY)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        runningFlow.value = false
    }

    private suspend fun increaseMillisCompletedTodayForTask(taskId: Long, amount: Long) {
        taskDao.increaseMillisCompletedToday(taskId, amount)
    }
}
