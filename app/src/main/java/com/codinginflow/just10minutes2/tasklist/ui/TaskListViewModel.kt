package com.codinginflow.just10minutes2.tasklist.ui

import androidx.lifecycle.*
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.containsWeekdayOfDate
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    private val selectedTask = taskTimerManager.selectedTask

    private val timerRunning = taskTimerManager.timerRunning

    private val activeDay = dayCheckPreferencesManager.activeDay

    val taskIdsActiveToday = combine(tasks, activeDay) { tasks, activeDay ->
        if (activeDay != null) {
            val activeTasks = mutableListOf<Long>()
            tasks.forEach { task ->
                if (task.weekdays.containsWeekdayOfDate(activeDay)) {
                    activeTasks.add(task.id)
                }
            }
            activeTasks
        } else emptyList()
    }

    private var pendingNewTask = savedStateHandle.get<Task>("pendingNewTask")
        set(value) {
            field = value
            savedStateHandle.set("pendingNewTask", value)
        }

    val runningTask = combine(selectedTask, timerRunning) { task, running ->
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
            val selectedTask = selectedTask.first()
            val timerRunning = timerRunning.first()

            if (task == selectedTask && timerRunning) return@launch

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
            timerPreferencesManager.updateSelectedTaskId(task.id)
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
        data class OpenTaskStatistics(val taskId: Long) : Event()
        object NavigateToArchive : Event()
        object NavigateToTimer : Event()
    }
}