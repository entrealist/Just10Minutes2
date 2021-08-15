package com.codinginflow.just10minutes2.statistics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.tasklist.ui.TaskListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val statisticsDao: TaskStatisticsDao
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val tasks = taskDao.getAllTasks()
    val taskStatistics = statisticsDao.getAllTaskStatistics()

    fun onTaskDetailsClicked(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.OpenTaskStatistics(task.id))
        }
    }

    sealed class Event {
        data class OpenTaskStatistics(val taskId: Long) : Event()
    }
}