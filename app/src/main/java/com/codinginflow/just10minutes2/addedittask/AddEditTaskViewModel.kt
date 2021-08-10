package com.codinginflow.just10minutes2.addedittask

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.ARG_TASK_ID
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val taskId = savedStateHandle.get<Long>(ARG_TASK_ID) ?: Task.NO_ID
    private var task: Task? = null

    private val taskNameInputLiveData = savedStateHandle.getLiveData<String>("taskTitleInput")
    val taskNameInput: LiveData<String> = taskNameInputLiveData

    private val minutesGoalInputLiveData = savedStateHandle.getLiveData<String>("minutesGoalInput")
    val minutesGoalInput: LiveData<String> = minutesGoalInputLiveData

    init {
        Timber.d("id = $taskId")
        if (taskId != Task.NO_ID) {
            loadTaskFromId(taskId)
        }
    }

    private fun loadTaskFromId(taskId: Long) {
        viewModelScope.launch {
            task = taskDao.getTaskById(taskId).first()
            val titleInput = taskNameInputLiveData.value
            if (titleInput == null) {
                taskNameInputLiveData.value = task?.name
            }
            val minutesGoalInput = minutesGoalInputLiveData.value
            if (minutesGoalInput == null) {
                minutesGoalInputLiveData.value =
                    task?.dailyGoalInMinutes?.toString()
            }
        }
    }

    fun onTaskNameInputChanged(input: String) {
        taskNameInputLiveData.value = input
    }

    fun onMinutesGoalInputChanged(input: String) {
        val digitInput = input.filter { it.isDigit() }
        if (digitInput.length <= 3) {
            minutesGoalInputLiveData.value = digitInput
        }
    }

    fun onSaveClicked() {
        val taskNameInput = taskNameInput.value
        val minutesGoalInput = minutesGoalInput.value

        if (taskNameInput.isNullOrBlank()) {
            showInvalidInputMessage(R.string.name_cant_be_empty)
            return
        }

        if (minutesGoalInput.isNullOrBlank()) {
            showInvalidInputMessage(R.string.please_set_minutes_goal)
            return
        }

        val minutesGoal = minutesGoalInput.toInt()

        if (minutesGoal < 1) {
            showInvalidInputMessage(R.string.minutes_goal_cant_be_zero)
        }

        if (taskId == Task.NO_ID) {
            val newTask = Task(name = taskNameInput, dailyGoalInMinutes = minutesGoal)
            createTask(newTask)
        } else {
            val task = task
            if (task != null) { // avoid save before task has loaded
                val updatedTask = task.copy(name = taskNameInput, dailyGoalInMinutes = minutesGoal)
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

    fun onDeleteClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskDeleted(task.id)))
        }
    }

    fun onNavigateUpClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateUp)
        }
    }

    private fun showInvalidInputMessage(@StringRes msg: Int) {
        viewModelScope.launch { eventChannel.send(Event.ShowInvalidInputMessage(msg)) }
    }

    sealed class Event {
        data class ShowInvalidInputMessage(@StringRes val msg: Int) : Event()
        data class NavigateBackWithResult(val result: AddEditTaskResult) : Event()
        object NavigateUp : Event()
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