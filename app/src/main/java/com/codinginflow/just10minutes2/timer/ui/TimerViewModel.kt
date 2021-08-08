package com.codinginflow.just10minutes2.timer.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,
    private val timerPreferencesManager: TimerPreferencesManager,
    savedState: SavedStateHandle
) : ViewModel() {

    private val timerPreferencesFlow = timerPreferencesManager.timerPreferencesFlow

    val allTasks = taskDao.getAllTasks()

    val selectedTask = timerPreferencesFlow.flatMapLatest { timerPreferences ->
        timerPreferences.activeTaskId?.let { taskId ->
            taskDao.getTaskById(taskId)
        } ?: emptyFlow()
    }

    init {
        viewModelScope.launch {
            selectedTask.collect { task ->
                if (task != null && task.millisLeftToday <= 0) stopTimer()
            }
        }
    }

    val timerRunning = taskTimerManager.running

    fun onTaskSelected(task: Task) {
        viewModelScope.launch {
            timerPreferencesManager.updateActiveTaskId(task.id)
        }
    }

    fun onStartTimerClicked() {
        viewModelScope.launch {
            val selectedTask = selectedTask.first()
            if (selectedTask != null) {
                taskTimerManager.startTimerForTask(selectedTask.id)
            }
        }
    }

    fun onStopTimerClicked() {
        stopTimer()
    }

    private fun stopTimer() {
        taskTimerManager.stopTimer()
    }
}