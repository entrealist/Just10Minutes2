package com.codinginflow.just10minutes2.tasklist.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskTimerManager: TaskTimerManager,

    ) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val tasks = taskDao.getAllTasks()

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
                    eventChannel.send(Event.ShowAddEditConfirmationMessage(R.string.task_created))
                is AddEditTaskViewModel.AddEditTaskResult.TaskUpdated ->
                    eventChannel.send(Event.ShowAddEditConfirmationMessage(R.string.task_updated))
                is AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    eventChannel.send(Event.ShowAddEditConfirmationMessage(R.string.task_deleted))
            }
        }
    }

    fun onOpenTimerForTaskClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.OpenTimerForTask(task))
        }
    }

    sealed class Event {
        object AddNewTask : Event()
        data class EditTask(val taskId: Long) : Event()
        data class ShowAddEditConfirmationMessage(@StringRes val msg: Int) : Event()
        data class OpenTimerForTask(val task: Task) : Event()
    }
}