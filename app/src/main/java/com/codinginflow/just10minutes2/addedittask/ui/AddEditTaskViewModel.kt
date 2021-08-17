package com.codinginflow.just10minutes2.addedittask.ui

import android.os.Parcelable
import androidx.lifecycle.*
import com.codinginflow.just10minutes2.application.ARG_TASK_ID
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.WeekdaySelection
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private val taskId = savedState.get<Long>(ARG_TASK_ID) ?: Task.NO_ID

    private val task = taskDao.getTaskById(taskId)

    val isEditMode = taskId != Task.NO_ID

    val isArchivedTask = task.map { it != null && it.archived }

    private val taskNameInputLiveData = savedState.getLiveData<String>("taskTitleInput")
    val taskNameInput: LiveData<String> = taskNameInputLiveData

    private val minutesGoalInputLiveData = savedState.getLiveData<String>("minutesGoalInput")
    val minutesGoalInput: LiveData<String> = minutesGoalInputLiveData

    private val weekdaysSelectionInputLiveData =
        savedState.getLiveData<WeekdaySelection>("weekdaySelection")
    val weekdaysSelectionInput: LiveData<WeekdaySelection> = weekdaysSelectionInputLiveData

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

    private val showUnarchiveTaskConfirmationDialogLiveData =
        savedState.getLiveData<Boolean>("showUnarchiveTaskConfirmationDialog")
    val showUnarchiveTaskConfirmationDialog: LiveData<Boolean> =
        showUnarchiveTaskConfirmationDialogLiveData

    init {
        viewModelScope.launch {
            val task = taskDao.getTaskById(taskId).first()
            if (task != null) {
                populateInputFieldsFromTaskIfEmpty(task)
            } else {
                weekdaysSelectionInputLiveData.value = WeekdaySelection(allDays = true)
            }
        }
    }

    private fun populateInputFieldsFromTaskIfEmpty(task: Task) {
        val titleInput = taskNameInputLiveData.value
        if (titleInput == null) {
            taskNameInputLiveData.value = task.name
        }
        val minutesGoalInput = minutesGoalInputLiveData.value
        if (minutesGoalInput == null) {
            minutesGoalInputLiveData.value =
                task.dailyGoalInMinutes.toString()
        }
        val weekdaysInput = weekdaysSelectionInputLiveData.value
        if (weekdaysInput == null) {
            weekdaysSelectionInputLiveData.value =
                task.weekdays
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

    fun onWeekdaysSelectionInputChanged(input: WeekdaySelection) {
        weekdaysSelectionInputLiveData.value = input
    }

    fun onSaveClicked() {
        val taskNameInput = taskNameInput.value
        val minutesGoalInput = minutesGoalInput.value?.toIntOrNull()
        val weekdaysSelectionInput = weekdaysSelectionInput.value

        taskNameInputErrorMessageLiveData.value = null
        minutesGoalInputErrorMessageLiveData.value = null


        if (!taskNameInput.isNullOrEmpty() && minutesGoalInput != null && minutesGoalInput > 0 && weekdaysSelectionInput != null) {
            viewModelScope.launch {
                if (taskId == Task.NO_ID) {
                    val newTask = Task(
                        name = taskNameInput,
                        dailyGoalInMinutes = minutesGoalInput,
                        weekdays = weekdaysSelectionInput
                    )
                    createTask(newTask)
                } else {
                    val task = task.first()
                    if (task != null) {
                        val updatedTask =
                            task.copy(
                                name = taskNameInput,
                                dailyGoalInMinutes = minutesGoalInput,
                                weekdays = weekdaysSelectionInput
                            )
                        updateTask(updatedTask)
                    }
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

    private suspend fun createTask(task: Task) {
        taskDao.insert(task)
        eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskCreated))
    }

    private suspend fun updateTask(task: Task) {
        taskDao.update(task)
        eventChannel.send(Event.NavigateBackWithResult(AddEditTaskResult.TaskUpdated))
    }

    fun onDeleteTaskClicked() {
        showDeleteTaskConfirmationDialogLiveData.value = true
    }

    fun onDeleteTaskConfirmed() {
        viewModelScope.launch {
            task.first()?.let { task ->
                taskTimerManager.stopTimerIfTaskIsSelected(task)
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
        showArchiveTaskConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            task.first()?.let { task ->
                taskTimerManager.stopTimerIfTaskIsSelected(task)
                taskDao.setArchivedState(task.id, true)
                eventChannel.send(Event.ShowArchiveTaskCompletedMessage)
            }
        }
    }

    fun onDismissArchiveTaskConfirmationDialog() {
        showArchiveTaskConfirmationDialogLiveData.value = false
    }

    fun onUnarchiveTaskClicked() {
        showUnarchiveTaskConfirmationDialogLiveData.value = true
    }

    fun onUnarchiveTaskConfirmed() {
        showUnarchiveTaskConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            task.first()?.let { task ->
                taskDao.setArchivedState(task.id, false)
                eventChannel.send(Event.ShowUnarchiveTaskCompletedMessage)
            }
        }
    }

    fun onDismissUnarchiveTaskConfirmationDialog() {
        showUnarchiveTaskConfirmationDialogLiveData.value = false
    }

    fun onResetDayClicked() {
        showResetDayConfirmationDialogLiveData.value = true
    }

    fun onResetDayConfirmed() {
        showResetDayConfirmationDialogLiveData.value = false
        viewModelScope.launch {
            task.first()?.let { task ->
                taskTimerManager.stopTimerIfTaskIsSelected(task)
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
        object ShowArchiveTaskCompletedMessage : Event()
        object ShowUnarchiveTaskCompletedMessage : Event()
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
    }
}