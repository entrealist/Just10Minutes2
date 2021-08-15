package com.codinginflow.just10minutes2.tasklist.ui

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val tasks = taskDao.getAllNotArchivedTasks()

    private val activeTask = taskTimerManager.activeTask
    private val timerRunning = taskTimerManager.timerRunning
    val runningTask = combine(activeTask, timerRunning) { task, running ->
        if (running && task != null) {
            task.id
        } else {
            null
        }
    }

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
                    eventChannel.send(Event.ShowAddEditScreenConfirmationMessage(R.string.task_created))
                is AddEditTaskViewModel.AddEditTaskResult.TaskUpdated ->
                    eventChannel.send(Event.ShowAddEditScreenConfirmationMessage(R.string.task_updated))
                is AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    eventChannel.send(Event.ShowAddEditScreenConfirmationMessage(R.string.task_deleted))
                is AddEditTaskViewModel.AddEditTaskResult.TaskArchived ->
                    eventChannel.send(Event.ShowAddEditScreenConfirmationMessage(R.string.task_archived))
            }
        }
    }

    fun onOpenTimerForTaskClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.OpenTimerForTask(task))
        }
    }

    fun onNavigateToArchiveClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToArchive)
        }
    }

    sealed class Event {
        object AddNewTask : Event()
        data class EditTask(val taskId: Long) : Event()
        data class ShowAddEditScreenConfirmationMessage(@StringRes val msg: Int) : Event()
        data class OpenTimerForTask(val task: Task) : Event()
        object NavigateToArchive: Event()
    }
}