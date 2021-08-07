package com.codinginflow.just10minutes2.ui.tasklist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codinginflow.just10minutes2.ui.data.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(

) : ViewModel() {

    val tasks = MutableLiveData(
        listOf(
            Task("Example Task 1"),
            Task("Example Task 2"),
            Task("Example Task 3"),
        )
    )
}