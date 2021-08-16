package com.codinginflow.just10minutes2.tasklist.ui

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,
    private val timerPreferencesManager: TimerPreferencesManager,
    private val dayCheckPreferencesManager: DayCheckPreferencesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val tasks = taskDao.getAllNotArchivedTasks()

    private val activeTask = taskTimerManager.activeTask

    private val timerRunning = taskTimerManager.timerRunning

    val activeDay = dayCheckPreferencesManager.activeDay

    private var pendingNewTask = savedStateHandle.get<Task>("pendingNewTask")
        set(value) {
            field = value
            savedStateHandle.set("pendingNewTask", value)
        }

    val runningTask = combine(activeTask, timerRunning) { task, running ->
        if (running && task != null) {
            task.id
        } else {
            null
        }
    }

    private val showStartTimerForNewTaskConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showStartTimerForNewTaskConfirmationDialog")
    val showStartTimerForNewTaskConfirmationDialog: LiveData<Boolean> =
        showStartTimerForNewTaskConfirmationDialogLiveData

    fun onAddNewTaskClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.AddNewTask)
        }
    }

    fun onEditTaskClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.EditTask(task.id))
        }
    }

    fun onAddEditResult(addEditResult: AddEditTaskViewModel.AddEditTaskResult) {
        viewModelScope.launch {
            when (addEditResult) {
                is AddEditTaskViewModel.AddEditTaskResult.TaskCreated ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_created))
                is AddEditTaskViewModel.AddEditTaskResult.TaskUpdated ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_updated))
                is AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_deleted))
                is AddEditTaskViewModel.AddEditTaskResult.TaskArchived ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_archived))
                else -> {}
            }
        }
    }

    fun onOpenTaskStatisticsClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.OpenTaskStatistics(task.id))
        }
    }

    fun onNavigateToArchiveClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToArchive)
        }
    }

    fun onStartTimerClicked(task: Task) {
        viewModelScope.launch {
            val activeTask = activeTask.first()
            val timerRunning = timerRunning.first()

            if (task == activeTask && timerRunning) return@launch

            if (!timerRunning) {
                startTimerForNewTask(task)
            } else {
                pendingNewTask = task
                showStartTimerForNewTaskConfirmationDialogLiveData.value = true
            }
        }
    }

    fun onStartTimerForNewTaskConfirmed() {
        showStartTimerForNewTaskConfirmationDialogLiveData.value = false
        pendingNewTask?.let {
            startTimerForNewTask(it)
            pendingNewTask = null
        }
    }

    fun onDismissStartTimerForNewTaskConfirmationDialog() {
        showStartTimerForNewTaskConfirmationDialogLiveData.value = false
        pendingNewTask = null
    }

    private fun startTimerForNewTask(task: Task) {
        taskTimerManager.stopTimer()
        viewModelScope.launch {
            timerPreferencesManager.updateActiveTaskId(task.id)
            taskTimerManager.startTimer()
            eventChannel.send(Event.NavigateToTimer)
        }
    }

    fun onStopTimerClicked() {
        taskTimerManager.stopTimer()
    }

    sealed class Event {
        object AddNewTask : Event()
        data class EditTask(val taskId: Long) : Event()
        data class ShowAddEditResultMessage(@StringRes val msg: Int) : Event()
        data class OpenTaskStatistics(val taskId: Long) : Event()
        object NavigateToArchive : Event()
        object NavigateToTimer : Event()
    }
}