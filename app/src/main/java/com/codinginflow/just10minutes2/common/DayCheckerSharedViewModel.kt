package com.codinginflow.just10minutes2.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.daos.DailyTaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.DailyTaskStatistic
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
    private val dailyTaskStatisticsDao: DailyTaskStatisticsDao
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
                if (currentDay > activeDay.first()) {
                    // integrate further check for reset time after 0 am
                    startNewDay(currentDay)
                }

                Timber.d("current time: $currentDay")
                Timber.d("current time: ${currentDay.time}")
                delay(10_000)
            }
        }
    }

    private suspend fun startNewDay(day: Date) {
        taskTimerManager.stopTimer()
        createDailyTaskStatistics(day)
        taskDao.resetMillisCompletedTodayForAllTasks()
        dayCheckPreferencesManager.updateActiveDay(day)
    }

    private suspend fun createDailyTaskStatistics(day: Date) {
        val timestamp = day.time
        val allNotArchivedTasks = taskDao.getAllNotArchivedTasks().first()
        allNotArchivedTasks.forEach { task ->
            val dailyTaskStatistic = DailyTaskStatistic(
                task.id,
                timestamp,
                task.dailyGoalInMinutes,
                task.timeCompletedTodayInMilliseconds
            )
            dailyTaskStatisticsDao.insert(dailyTaskStatistic)
        }
    }
}