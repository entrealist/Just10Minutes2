package com.codinginflow.just10minutes2.timer.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,
    private val timerPreferencesManager: TimerPreferencesManager,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val activeTask = taskTimerManager.activeTask

    val allTasks = taskDao.getAllTasks()

    private var pendingNewTask = savedState.get<Task>("pendingNewTask")
        set(value) {
            field = value
            savedState.set("pendingNewTask", value)
        }

    val timerRunning = taskTimerManager.timerRunning

    fun onNewTaskSelected(newTask: Task) {
        viewModelScope.launch {
            val timerRunning = timerRunning.first()
            if (!timerRunning) {
                changeActiveTask(newTask)
            } else {
                pendingNewTask = newTask
                eventChannel.send(Event.ShowNewTaskSelectionConfirmationDialog)
            }
        }
    }

    fun onSelectNewTaskConfirmed() {
        pendingNewTask?.let {
            changeActiveTask(it)
            pendingNewTask = null
            viewModelScope.launch {
                eventChannel.send(Event.ShowTimerStoppedMessage)
            }
        }
    }

    fun onSelectNewTaskCanceled() {
        pendingNewTask = null
    }

    private fun changeActiveTask(task: Task) {
        stopTimer()
        viewModelScope.launch {
            timerPreferencesManager.updateActiveTaskId(task.id)
        }
    }

    fun onStartTimerClicked() {
        viewModelScope.launch {
            taskTimerManager.startTimer()
            eventChannel.send(Event.StartTimerService)
        }
    }

    fun onStopTimerClicked() {
        stopTimer()
    }

    private fun stopTimer() {
        viewModelScope.launch {
            val timerRunning = timerRunning.first()
            if (timerRunning) {
                taskTimerManager.stopTimer()
                eventChannel.send(Event.StopTimerService)
                eventChannel.send(Event.ShowTimerStoppedMessage)
            }
        }
    }

    sealed class Event {
        object StartTimerService : Event()
        object StopTimerService : Event()
        object ShowNewTaskSelectionConfirmationDialog : Event()
        object ShowTimerStoppedMessage : Event()
    }
}