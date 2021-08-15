package com.codinginflow.just10minutes2.archive.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.tasklist.ui.TaskListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val archivedTasks = taskDao.getAllArchivedTasks()

    private var pendingTaskIdToBeUnarchived =
        savedStateHandle.get<Long>("pendingTaskIdToBeUnarchived")
        set(value) {
            field = value
            savedStateHandle.set("pendingTaskIdToBeUnarchived", value)
        }

    fun onEditTaskClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.EditTask(task.id))
        }
    }

    fun onEditResult(addEditResult: AddEditTaskViewModel.AddEditTaskResult) {
        viewModelScope.launch {
            when (addEditResult) {
                is AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_deleted))
                is AddEditTaskViewModel.AddEditTaskResult.TaskUnarchived ->
                    eventChannel.send(Event.ShowAddEditResultMessage(R.string.task_unarchived))
                else -> {}
            }
        }
    }

    fun onOpenTaskStatisticsClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.OpenTaskStatistics(task.id))
        }
    }

    fun onNavigateUpClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateUp)
        }
    }

    sealed class Event {
        data class EditTask(val taskId: Long) : Event()
        data class ShowAddEditResultMessage(@StringRes val msg: Int) : Event()
        data class OpenTaskStatistics(val taskId: Long) : Event()
        object NavigateUp : Event()
        object ShowUnarchivedConfirmationMessage : Event()
    }
}