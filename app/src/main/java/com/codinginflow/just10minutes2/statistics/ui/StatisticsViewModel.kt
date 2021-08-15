package com.codinginflow.just10minutes2.statistics.ui

import androidx.lifecycle.ViewModel
import com.codinginflow.just10minutes2.common.data.daos.DailyTaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val statisticsDao: DailyTaskStatisticsDao
) : ViewModel() {

    val tasks = taskDao.getAllTasks()
    val taskStatistics = statisticsDao.getAllTaskStatistics()

    fun onTaskDetailsClicked(task: Task) {

    }
}