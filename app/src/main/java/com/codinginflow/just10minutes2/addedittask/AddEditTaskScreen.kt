package com.codinginflow.just10minutes2.addedittask

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
    navigateBackWithResult: (AddEditTaskViewModel.AddEditTaskResult) -> Unit,
) {
    val taskNameInput by viewModel.taskNameInput.observeAsState()
    val minutesGoalInput by viewModel.minutesGoalInput.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateBackWithResult -> TODO()
                is AddEditTaskViewModel.Event.ShowInvalidInputMessage -> TODO()
                is AddEditTaskViewModel.Event.NavigateUp -> navigateUp()
            }
        }
    }

    AddEditTaskBody(
        isEditMode = viewModel.taskId != Task.NO_ID,
        taskNameInput = taskNameInput,
        onTaskNameInputChanged = viewModel::onTaskNameInputChanged,
        minutesGoalInput = minutesGoalInput,
        onMinutesGoalInputChanged = viewModel::onMinutesGoalInputChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onNavigateUpClick = viewModel::onNavigateUpClicked
    )
}

@Composable
private fun AddEditTaskBody(
    isEditMode: Boolean,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    val title =
        stringResource(if (isEditMode) R.string.edit_task else R.string.add_task)

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = onSaveClicked) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = stringResource(R.string.save_changes)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUpClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            taskNameInput = taskNameInput,
            onTaskNameInputChanged = onTaskNameInputChanged,
            minutesGoalInput = minutesGoalInput,
            onMinutesGoalInputChanged = onMinutesGoalInputChanged,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(8.dp)) {
        OutlinedTextField(
            value = taskNameInput.orEmpty(),
            onValueChange = onTaskNameInputChanged,
            label = { Text(stringResource(R.string.task_name)) },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = minutesGoalInput.orEmpty(),
            onValueChange = onMinutesGoalInputChanged,
            label = { Text(stringResource(R.string.minutes_per_day)) },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun PreviewTaskListScreen() {
    Just10Minutes2Theme {
        AddEditTaskBody(
            isEditMode = false,
            taskNameInput = null,
            onTaskNameInputChanged = {},
            minutesGoalInput = "10",
            onMinutesGoalInputChanged = {},
            onSaveClicked = {},
            onNavigateUpClick = {}
        )
    }
}