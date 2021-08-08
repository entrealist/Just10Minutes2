package com.codinginflow.just10minutes2.timer.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.codinginflow.just10minutes2.common.data.TimerPreferencesManager
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.timer.TaskTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    taskDao: TaskDao,
    timerManager: TaskTimerManager,
    timerPreferencesManager: TimerPreferencesManager,
    savedState: SavedStateHandle
) : ViewModel() {

    private val timerPreferencesFlow = timerPreferencesManager.timerPreferencesFlow

    private val selectedTaskId = timerPreferencesFlow.flatMapLatest { timerPreferences ->
        taskDao.getTaskById(timerPreferences.activeTaskId)
    }
}