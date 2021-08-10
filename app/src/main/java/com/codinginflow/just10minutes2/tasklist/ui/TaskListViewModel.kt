package com.codinginflow.just10minutes2.tasklist.ui

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val tasks = taskDao.getAllTasks()

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
                    eventChannel.send(Event.ShowTaskSavedConfirmationMessage(R.string.task_created))
                is AddEditTaskViewModel.AddEditTaskResult.TaskUpdated ->
                    eventChannel.send(Event.ShowTaskSavedConfirmationMessage(R.string.task_updated))
                is AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    taskDao.getTaskById(addEditResult.taskId).first()?.let { task ->
                        taskDao.delete(task)
                        eventChannel.send(Event.ShowUndoDeleteTaskMessage(task))
                    }
            }
        }
    }

    fun onUndoDeleteTaskClicked(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    sealed class Event {
        object AddNewTask : Event()
        data class EditTask(val taskId: Long) : Event()
        data class ShowTaskSavedConfirmationMessage(@StringRes val msg: Int) : Event()
        data class ShowUndoDeleteTaskMessage(val task: Task) : Event()
    }
}