package com.codinginflow.just10minutes2.tasklist.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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

    sealed class Event {
        object AddNewTask : Event()
        data class EditTask(val taskId: Long) : Event()
    }
}