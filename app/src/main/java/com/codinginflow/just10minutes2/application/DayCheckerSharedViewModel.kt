package com.codinginflow.just10minutes2.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.util.getDateWithoutTime
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DayCheckerSharedViewModel @Inject constructor(
    private val dayCheckPreferencesManager: DayCheckPreferencesManager,
    private val taskTimerManager: TaskTimerManager,
    private val taskDao: TaskDao,
    private val taskStatisticsDao: TaskStatisticsDao
) : ViewModel() {

    private val activeDay = dayCheckPreferencesManager.activeDay

    init {
        runDayChecker()
    }

    private fun runDayChecker() {
        viewModelScope.launch {
            activeDay.collect { activeDay ->
                Timber.d("activeDay: $activeDay")
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(20000)
                val currentDay = Calendar.getInstance().getDateWithoutTime(Date())
                val activeDay = activeDay.first()
                if (currentDay > activeDay) {
                    // integrate further check for reset time after 0 am
                    startNewDay(previousDay = activeDay, newDay = currentDay)
                }

                Timber.d("current time: $currentDay")
                Timber.d("current time: ${currentDay.time}")
                delay(10_000)
            }
        }
    }

    private suspend fun startNewDay(previousDay: Date, newDay: Date) {
        Timber.d("starting new day")
        taskTimerManager.stopTimer()
        createDailyTaskStatistics(previousDay)
        taskDao.resetMillisCompletedTodayForAllTasks()
        dayCheckPreferencesManager.updateActiveDay(newDay)
    }

    private suspend fun createDailyTaskStatistics(day: Date) {
        val timestamp = day.time
        val allNotArchivedTasks = taskDao.getAllNotArchivedTasks().first()
        allNotArchivedTasks.forEach { task ->
            val dailyTaskStatistic = TaskStatistic(
                task.id,
                timestamp,
                task.dailyGoalInMinutes,
                task.timeCompletedTodayInMilliseconds
            )
            taskStatisticsDao.insert(dailyTaskStatistic)
        }
    }
}