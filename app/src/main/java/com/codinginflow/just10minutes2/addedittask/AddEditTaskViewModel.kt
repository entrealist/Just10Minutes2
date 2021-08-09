package com.codinginflow.just10minutes2.addedittask

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val taskId = savedStateHandle.get<Long>("taskId") ?: Task.NO_ID

    private val taskLivedata = MutableLiveData<Task?>()
    val task: LiveData<Task?> = taskLivedata

    private val nameInputLiveData = savedStateHandle.getLiveData<String>("taskTitleInput")
    val nameInput: LiveData<String> = nameInputLiveData

    init {
        if (taskId != Task.NO_ID) {
            loadTaskFromId(taskId)
        }
    }

    private fun loadTaskFromId(taskId: Long) {
        viewModelScope.launch {
            val task = taskDao.getTaskById(taskId).first()
            taskLivedata.value = task
            val titleInput = nameInputLiveData.value
            if (titleInput != null) {
                nameInputLiveData.value = task?.name
            }
        }
    }

    fun setTaskName(name: String) {
        nameInputLiveData.value = name
    }

    fun onSaveClick() {
        val name = nameInput.value

        if (name.isNullOrBlank()) {
            showInvalidInputMessage(R.string.name_cant_be_empty)
            return
        }

        if (taskId == Task.NO_ID) {
            val newTask = Task(name = name)
            createTask(newTask)
        } else {
            val task = task.value
            if (task != null) { // avoid save before task has loaded
                val updatedTask = task.copy(name = name)
                updateTask(updatedTask)
            }
        }
    }

    private fun createTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
            eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskCreated))
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
            eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskUpdated))
        }
    }

    fun onDeleteClick(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskDeleted(task.id)))
        }
    }

    private fun showInvalidInputMessage(@StringRes msg: Int) {
        viewModelScope.launch { eventChannel.send(Event.ShowInvalidInputMessage(msg)) }
    }

    sealed class Event {
        data class ShowInvalidInputMessage(@StringRes val msg: Int) : Event()
        data class NavigateBackWithResult(val result: AddEditTaskResult) : Event()
    }

    sealed class AddEditTaskResult : Parcelable {
        @Parcelize
        object TaskCreated : AddEditTaskResult()

        @Parcelize
        object TaskUpdated : AddEditTaskResult()

        @Parcelize
        data class TaskDeleted(val taskId: Long) : AddEditTaskResult()
    }
}