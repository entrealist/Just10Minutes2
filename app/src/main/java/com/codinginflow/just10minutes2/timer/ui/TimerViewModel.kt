package com.codinginflow.just10minutes2.timer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val activeTask = taskTimerManager.activeTask

    val allTasks = taskDao.getAllNotArchivedTasks()

    val timerRunning = taskTimerManager.timerRunning

    private var pendingNewTask = savedStateHandle.get<Task>("pendingNewTask")
        set(value) {
            field = value
            savedStateHandle.set("pendingNewTask", value)
        }

    private val showSelectNewTaskConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showSelectNewTaskConfirmationDialog")
    val showSelectNewTaskConfirmationDialog: LiveData<Boolean> = showSelectNewTaskConfirmationDialogLiveData

    fun onTaskSelected(newTask: Task) {
        viewModelScope.launch {
            val activeTask = activeTask.first()
            if (activeTask == newTask) return@launch

            val timerRunning = timerRunning.first()
            if (!timerRunning) {
                changeActiveTask(newTask)
            } else {
                pendingNewTask = newTask
                showSelectNewTaskConfirmationDialogLiveData.value = true
            }
        }
    }

    fun onSelectNewTaskConfirmed() {
        showSelectNewTaskConfirmationDialogLiveData.value = false
        pendingNewTask?.let {
            changeActiveTask(it)
            pendingNewTask = null
            viewModelScope.launch {
                eventChannel.send(Event.ShowTimerStoppedMessage)
            }
        }
    }

    fun onDismissSelectNewTaskConfirmationDialog() {
        showSelectNewTaskConfirmationDialogLiveData.value = false
        pendingNewTask = null
    }

    private fun changeActiveTask(task: Task) {
        taskTimerManager.stopTimer()
        viewModelScope.launch {
            timerPreferencesManager.updateActiveTaskId(task.id)
        }
    }

    fun onStartTimerClicked() = taskTimerManager.startTimer()

    fun onStopTimerClicked() = taskTimerManager.stopTimer()

    sealed class Event {
        object ShowTimerStoppedMessage : Event()
    }
}