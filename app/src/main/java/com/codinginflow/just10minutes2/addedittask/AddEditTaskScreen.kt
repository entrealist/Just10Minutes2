package com.codinginflow.just10minutes2.addedittask

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateBackWithResult: (AddEditTaskViewModel.AddEditTaskResult) -> Unit,
) {
    val task by viewModel.task.observeAsState()

    Text("id: ${task?.id}")
}

@Composable
private fun AddEditTaskBody() {

}