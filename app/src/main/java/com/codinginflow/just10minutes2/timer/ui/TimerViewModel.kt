package com.codinginflow.just10minutes2.timer.ui

import androidx.lifecycle.*
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.containsWeekdayOfDate
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val selectedTask: Task?,
    val taskActiveToday: Boolean,
    val allTasks: List<Task>,
    val timerRunning: Boolean,
    val showSelectNewTaskConfirmationDialog: Boolean
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,
    private val timerPreferencesManager: TimerPreferencesManager,
    private val dayCheckPreferencesManager: DayCheckPreferencesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val selectedTask = taskTimerManager.selectedTask
    private val activeDay = dayCheckPreferencesManager.activeDay
    private val allTasks = taskDao.getAllNotArchivedTasks()
    private val timerRunning = taskTimerManager.timerRunning

    private val showSelectNewTaskConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>("showSelectNewTaskConfirmationDialog", false)

    val uiState = combine(
        selectedTask,
        activeDay,
        allTasks,
        timerRunning,
        showSelectNewTaskConfirmationDialog.asFlow()
    ) { selectedTask, activeDay, allTasks, timerRunning, showSelectNewTaskConfirmationDialog ->
        TimerUiState(
            selectedTask = selectedTask,
            taskActiveToday = selectedTask != null && activeDay != null && selectedTask.weekdays.containsWeekdayOfDate(
                activeDay
            ),
            allTasks = allTasks,
            timerRunning = timerRunning,
            showSelectNewTaskConfirmationDialog = showSelectNewTaskConfirmationDialog
        )
    }

    private var pendingNewTask = savedStateHandle.get<Task>("pendingNewTask")
        set(value) {
            field = value
            savedStateHandle.set("pendingNewTask", value)
        }


    fun onTaskSelected(newTask: Task) {
        viewModelScope.launch {
            val selectedTask = selectedTask.first()
            if (selectedTask == newTask) return@launch

            val timerRunning = timerRunning.first()
            if (!timerRunning) {
                changeSelectedTaskTask(newTask)
            } else {
                pendingNewTask = newTask
                showSelectNewTaskConfirmationDialog.value = true
            }
        }
    }

    fun onSelectNewTaskConfirmed() {
        showSelectNewTaskConfirmationDialog.value = false
        pendingNewTask?.let {
            changeSelectedTaskTask(it)
            pendingNewTask = null
            viewModelScope.launch {
                eventChannel.send(Event.ShowTimerStoppedMessage)
            }
        }
    }

    fun onDismissSelectNewTaskConfirmationDialog() {
        showSelectNewTaskConfirmationDialog.value = false
        pendingNewTask = null
    }

    private fun changeSelectedTaskTask(task: Task) {
        taskTimerManager.stopTimer()
        viewModelScope.launch {
            timerPreferencesManager.updateSelectedTaskId(task.id)
        }
    }

    fun onEditTaskClicked() {
        viewModelScope.launch {
            val selectedTask = selectedTask.first()
            selectedTask?.let {
                eventChannel.send(Event.EditTask(selectedTask.id))
            }
        }
    }


    fun onStartTimerClicked() = taskTimerManager.startTimer()

    fun onStopTimerClicked() = taskTimerManager.stopTimer()

    sealed class Event {
        object ShowTimerStoppedMessage : Event()
        data class EditTask(val taskId: Long) : Event()
    }
}