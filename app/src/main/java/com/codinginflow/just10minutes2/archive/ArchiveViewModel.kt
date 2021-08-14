package com.codinginflow.just10minutes2.archive

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.addedittask.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
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

    private val showUnarchiveTaskConfirmationDialogLiveData =
        savedStateHandle.getLiveData<Boolean>("showUnarchiveTaskConfirmationDialog")
    val showUnarchiveTaskConfirmationDialog: LiveData<Boolean> =
        showUnarchiveTaskConfirmationDialogLiveData

    fun onUnarchiveTaskClicked(task: Task) {
        pendingTaskIdToBeUnarchived = task.id
        showUnarchiveTaskConfirmationDialogLiveData.value = true
    }

    fun onArchiveTaskConfirmed() {
        showUnarchiveTaskConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            pendingTaskIdToBeUnarchived?.let { id ->
                taskDao.setArchivedState(id, false)
                pendingTaskIdToBeUnarchived = null
                eventChannel.send(Event.ShowUnarchivedConfirmationMessage)
            }
        }
    }

    fun onDismissArchiveTaskConfirmationDialog() {
        pendingTaskIdToBeUnarchived = null
        showUnarchiveTaskConfirmationDialogLiveData.value = false
    }

    fun onNavigateUpClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateUp)
        }
    }

    sealed class Event {
        object NavigateUp : Event()
        object ShowUnarchivedConfirmationMessage : Event()
    }
}