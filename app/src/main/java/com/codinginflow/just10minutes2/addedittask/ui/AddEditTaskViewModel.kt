package com.codinginflow.just10minutes2.addedittask.ui

import android.os.Parcelable
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.ARG_TASK_ID
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.TaskTimerManager
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
    private val taskTimerManager: TaskTimerManager,
    savedState: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val taskId = savedState.get<Long>(ARG_TASK_ID) ?: Task.NO_ID
    private var task: Task? = null

    private val taskNameInputLiveData = savedState.getLiveData<String>("taskTitleInput")
    val taskNameInput: LiveData<String> = taskNameInputLiveData

    private val minutesGoalInputLiveData = savedState.getLiveData<String>("minutesGoalInput")
    val minutesGoalInput: LiveData<String> = minutesGoalInputLiveData

    private val taskNameInputErrorMessageLiveData =
        savedState.getLiveData<Int>("taskNameInputErrorMessage")
    val taskNameInputErrorMessage: LiveData<Int> = taskNameInputErrorMessageLiveData

    private val minutesGoalInputErrorMessageLiveData =
        savedState.getLiveData<Int>("minutesGoalInputErrorMessage")
    val minutesGoalInputErrorMessage: LiveData<Int> = minutesGoalInputErrorMessageLiveData

    private val showDeleteTaskConfirmationDialogLiveData =
        savedState.getLiveData<Boolean>("showDeleteTaskConfirmationDialog")
    val showDeleteTaskConfirmationDialog: LiveData<Boolean> =
        showDeleteTaskConfirmationDialogLiveData

    private val showResetDayConfirmationDialogLiveData =
        savedState.getLiveData<Boolean>("showResetDayConfirmationDialog")
    val showResetDayConfirmationDialog: LiveData<Boolean> = showResetDayConfirmationDialogLiveData

    private val showArchiveTaskConfirmationDialogLiveData =
        savedState.getLiveData<Boolean>("showArchiveTaskConfirmationDialog")
    val showArchiveTaskConfirmationDialog: LiveData<Boolean> =
        showArchiveTaskConfirmationDialogLiveData

    init {
        if (taskId != Task.NO_ID) {
            viewModelScope.launch {
                task = taskDao.getNotArchivedTaskById(taskId).first()
                populateInputFieldsFromTask()
            }
        }
    }

    private fun populateInputFieldsFromTask() {
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
        val minutesGoalInput = minutesGoalInput.value?.toIntOrNull()

        taskNameInputErrorMessageLiveData.value = null
        minutesGoalInputErrorMessageLiveData.value = null

        if (!taskNameInput.isNullOrEmpty() && minutesGoalInput != null && minutesGoalInput > 0) {
            if (taskId == Task.NO_ID) {
                val newTask = Task(name = taskNameInput, dailyGoalInMinutes = minutesGoalInput)
                createTask(newTask)
            } else {
                val task = task
                if (task != null) {
                    val updatedTask =
                        task.copy(name = taskNameInput, dailyGoalInMinutes = minutesGoalInput)
                    updateTask(updatedTask)
                }
            }
        } else {
            if (taskNameInput.isNullOrBlank()) {
                taskNameInputErrorMessageLiveData.value = R.string.task_name_empty_error
            }
            if (minutesGoalInput == null) {
                minutesGoalInputErrorMessageLiveData.value = R.string.minutes_goal_empty_error
            } else if (minutesGoalInput == 0) {
                minutesGoalInputErrorMessageLiveData.value = R.string.minutes_goal_zero_error
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

    fun onDeleteTaskClicked() {
        showDeleteTaskConfirmationDialogLiveData.value = true
    }

    fun onDeleteTaskConfirmed() {
        viewModelScope.launch {
            task?.let { task ->
                taskTimerManager.stopTimerIfTaskIsActive(task)
                taskDao.deleteTask(task)
                eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskDeleted))
            }
        }
    }

    fun onDismissDeleteTaskConfirmationDialog() {
        showDeleteTaskConfirmationDialogLiveData.value = false
    }

    fun onArchiveTaskClicked() {
        showArchiveTaskConfirmationDialogLiveData.value = true
    }

    fun onArchiveTaskConfirmed() {
        viewModelScope.launch {
            task?.let { task ->
                taskTimerManager.stopTimerIfTaskIsActive(task)
                taskDao.setArchivedState(task.id, true)
                eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskArchived))
            }
        }
    }

    fun onDismissArchiveTaskConfirmationDialog() {
        showArchiveTaskConfirmationDialogLiveData.value = false
    }

    fun onResetDayClicked() {
        showResetDayConfirmationDialogLiveData.value = true
    }

    fun onResetDayConfirmed() {
        showResetDayConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            task?.let { task ->
                taskTimerManager.stopTimerIfTaskIsActive(task)
                taskDao.resetMillisCompletedTodayForTask(task.id)
                eventChannel.send(Event.ShowResetDayCompletedMessage)
            }
        }
    }

    fun onDismissResetDayConfirmationDialog() {
        showResetDayConfirmationDialogLiveData.value = false
    }

    fun onNavigateUpClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateUp)
        }
    }

    sealed class Event {
        object ShowResetDayCompletedMessage : Event()
        data class NavigateBackWithResult(val result: AddEditTaskResult) : Event()
        object NavigateUp : Event()
    }

    sealed class AddEditTaskResult : Parcelable {
        @Parcelize
        object TaskCreated : AddEditTaskResult()

        @Parcelize
        object TaskUpdated : AddEditTaskResult()

        @Parcelize
        object TaskDeleted : AddEditTaskResult()

        @Parcelize
        object TaskArchived : AddEditTaskResult()
    }
}