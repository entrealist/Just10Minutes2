package com.codinginflow.just10minutes2.timer.ui

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.archive.ui.ArchiveViewModel
import com.codinginflow.just10minutes2.common.data.preferences.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.containsWeekdayOfDate
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.tasklist.ui.TaskListViewModel
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val activeTask: Task?,
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

    private val activeTask = taskTimerManager.activeTask
    private val activeDay = dayCheckPreferencesManager.activeDay
    private val allTasks = taskDao.getAllNotArchivedTasks()
    private val timerRunning = taskTimerManager.timerRunning

    private val showSelectNewTaskConfirmationDialog =
        savedStateHandle.getLiveData<Boolean>("showSelectNewTaskConfirmationDialog", false)

    val uiState = combine(
        activeTask,
        activeDay,
        allTasks,
        timerRunning,
        showSelectNewTaskConfirmationDialog.asFlow()
    ) { activeTask, activeDay, allTasks, timerRunning, showSelectNewTaskConfirmationDialog ->
        TimerUiState(
            activeTask = activeTask,
            taskActiveToday = activeTask != null && activeDay != null && activeTask.weekdays.containsWeekdayOfDate(
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
            val activeTask = activeTask.first()
            if (activeTask == newTask) return@launch

            val timerRunning = timerRunning.first()
            if (!timerRunning) {
                changeActiveTask(newTask)
            } else {
                pendingNewTask = newTask
                showSelectNewTaskConfirmationDialog.value = true
            }
        }
    }

    fun onSelectNewTaskConfirmed() {
        showSelectNewTaskConfirmationDialog.value = false
        pendingNewTask?.let {
            changeActiveTask(it)
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

    private fun changeActiveTask(task: Task) {
        taskTimerManager.stopTimer()
        viewModelScope.launch {
            timerPreferencesManager.updateActiveTaskId(task.id)
        }
    }

    fun onEditTaskClicked() {
        viewModelScope.launch {
            val activeTask = activeTask.first()
            activeTask?.let {
                eventChannel.send(Event.EditTask(activeTask.id))
            }
        }
    }

    fun onEditResult(addEditResult: AddEditTaskViewModel.AddEditTaskResult) {
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

    fun onStartTimerClicked() = taskTimerManager.startTimer()

    fun onStopTimerClicked() = taskTimerManager.stopTimer()

    sealed class Event {
        object ShowTimerStoppedMessage : Event()
        data class EditTask(val taskId: Long) : Event()
        data class ShowAddEditResultMessage(@StringRes val msg: Int) : Event()
    }
}