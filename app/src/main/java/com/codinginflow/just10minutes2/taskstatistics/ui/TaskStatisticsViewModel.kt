package com.codinginflow.just10minutes2.taskstatistics.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.ARG_TASK_ID
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskStatisticsViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val taskStatisticsDao: TaskStatisticsDao,
    savedState: SavedStateHandle,
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val taskId = savedState.get<Long>(ARG_TASK_ID) ?: Task.NO_ID

    val task = taskDao.getTaskById(taskId)
    val taskStatistics = taskStatisticsDao.getTaskStatisticsForTaskId(taskId)

    fun onNavigateUpClicked() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateUp)
        }
    }

    sealed class Event {
        object NavigateUp: Event()
    }
}