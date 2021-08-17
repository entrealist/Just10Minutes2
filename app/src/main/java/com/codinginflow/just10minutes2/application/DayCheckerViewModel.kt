package com.codinginflow.just10minutes2.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.data.entities.containsWeekdayOfDate
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.util.getDateWithoutTime
import com.codinginflow.just10minutes2.notification.NotificationHelper
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

private const val DAY_CHECK_INTERVAL = 10_000L

@HiltViewModel
class DayCheckerViewModel @Inject constructor(
    private val dayCheckPreferencesManager: DayCheckPreferencesManager,
    private val taskTimerManager: TaskTimerManager,
    private val taskDao: TaskDao,
    private val taskStatisticsDao: TaskStatisticsDao,
    private val notificationHelper: NotificationHelper
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
                val currentDay = Calendar.getInstance().getDateWithoutTime(Date())
                val activeDay = activeDay.first()?.time
                if (activeDay == null || currentDay != activeDay) {
                    // integrate further check for reset time after 0 am
                    startNewDay(previousDay = activeDay, newDay = currentDay)
                }

                Timber.d("current time: $currentDay")
                Timber.d("current time: ${currentDay.time}")
                delay(DAY_CHECK_INTERVAL)
            }
        }
    }

    private suspend fun startNewDay(previousDay: Date?, newDay: Date) {
        taskTimerManager.stopTimer()
        if (previousDay != null && newDay > previousDay) {
            createDailyTaskStatistics(previousDay)
        }
        taskDao.resetMillisCompletedTodayForAllTasks()
        dayCheckPreferencesManager.updateActiveDay(newDay)
        notificationHelper.showNewDayNotification()
    }

    private suspend fun createDailyTaskStatistics(day: Date) {
        val timestamp = day.time
        val cal = Calendar.getInstance().apply { time = day }
        val allNotArchivedTasks = taskDao.getAllNotArchivedTasks().first()
        val archivedTasksWithTimeProgressToday = taskDao.getAllArchivedTasksWithTimeProgressToday().first()
        val activeTasksToday = (allNotArchivedTasks + archivedTasksWithTimeProgressToday).filter { it.weekdays.containsWeekdayOfDate(cal) }
        activeTasksToday.forEach { task ->
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