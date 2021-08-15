package com.codinginflow.just10minutes2.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.preferences.DayCheckPreferencesManager
import com.codinginflow.just10minutes2.common.util.getDateWithoutTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimerSharedViewModel @Inject constructor() : ViewModel() {

    private val taskIdToOpenInTimerChannel = Channel<Task>()
    val taskToOpenInTimer = taskIdToOpenInTimerChannel.receiveAsFlow()

    fun setTaskIdToOpenInTimer(task: Task) {
        viewModelScope.launch {
            taskIdToOpenInTimerChannel.send(task)
        }
    }
}