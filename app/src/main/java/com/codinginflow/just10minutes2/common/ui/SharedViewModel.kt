package com.codinginflow.just10minutes2.common.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(): ViewModel() {

    private val taskIdToOpenInTimerChannel = Channel<Task>()
    val taskToOpenInTimer = taskIdToOpenInTimerChannel.receiveAsFlow()

    fun setTaskIdToOpenInTimer(task: Task) {
        viewModelScope.launch {
            taskIdToOpenInTimerChannel.send(task)
        }
    }
}