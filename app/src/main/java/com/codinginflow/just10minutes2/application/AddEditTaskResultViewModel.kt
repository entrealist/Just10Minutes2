package com.codinginflow.just10minutes2.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskResultViewModel @Inject constructor() : ViewModel() {

    private val resultMessageChannel = Channel<Int>()
    val resultMessage = resultMessageChannel.receiveAsFlow()

    fun onAddEditTaskResult(addEditTaskResult: AddEditTaskViewModel.AddEditTaskResult) {
        viewModelScope.launch {
            when (addEditTaskResult) {
                AddEditTaskViewModel.AddEditTaskResult.TaskCreated ->
                    resultMessageChannel.send(R.string.task_created)
                AddEditTaskViewModel.AddEditTaskResult.TaskUpdated ->
                    resultMessageChannel.send(R.string.task_updated)
                AddEditTaskViewModel.AddEditTaskResult.TaskDeleted ->
                    resultMessageChannel.send(R.string.task_deleted)
            }
        }
    }
}