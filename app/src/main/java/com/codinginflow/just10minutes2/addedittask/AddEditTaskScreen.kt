package com.codinginflow.just10minutes2.addedittask

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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

    val taskNameInputErrorMessage by viewModel.taskNameInputErrorMessage.observeAsState()
    val minutesGoalInputErrorMessage by viewModel.minutesGoalInputErrorMessage.observeAsState()

    val scaffoldState = rememberScaffoldState()

    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateBackWithResult ->
                    navigateBackWithResult(event.result)
                is AddEditTaskViewModel.Event.ShowDeleteConfirmationDialog ->
                    showDeleteConfirmationDialog = true
                is AddEditTaskViewModel.Event.NavigateUp -> navigateUp()
            }
        }
    }

    AddEditTaskBody(
        isEditMode = viewModel.taskId != Task.NO_ID,
        taskNameInput = taskNameInput,
        onTaskNameInputChanged = viewModel::onTaskNameInputChanged,
        taskNameInputErrorMessage = taskNameInputErrorMessage,
        minutesGoalInput = minutesGoalInput,
        onMinutesGoalInputChanged = viewModel::onMinutesGoalInputChanged,
        minutesGoalInputErrorMessage = minutesGoalInputErrorMessage,
        onSaveClicked = viewModel::onSaveClicked,
        onDeleteClicked = viewModel::onDeleteClicked,
        onNavigateUpClick = viewModel::onNavigateUpClicked,
        showDeleteConfirmationDialog = showDeleteConfirmationDialog,
        onDismissDeleteConfirmationDialog = { showDeleteConfirmationDialog = false },
        onDeleteConfirmed = viewModel::onDeletionConfirmed,
        scaffoldState = scaffoldState
    )
}

@Composable
private fun AddEditTaskBody(
    isEditMode: Boolean,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    @StringRes taskNameInputErrorMessage: Int?,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    @StringRes minutesGoalInputErrorMessage: Int?,
    onSaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onNavigateUpClick: () -> Unit,
    showDeleteConfirmationDialog: Boolean,
    onDismissDeleteConfirmationDialog: () -> Unit,
    onDeleteConfirmed: () -> Unit,
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
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            isEditMode = isEditMode,
            taskNameInput = taskNameInput,
            onTaskNameInputChanged = onTaskNameInputChanged,
            taskNameInputErrorMessage = taskNameInputErrorMessage,
            minutesGoalInput = minutesGoalInput,
            onMinutesGoalInputChanged = onMinutesGoalInputChanged,
            minutesGoalInputErrorMessage = minutesGoalInputErrorMessage,
            onDeleteClicked = onDeleteClicked,
            modifier = Modifier.padding(innerPadding)
        )

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = onDismissDeleteConfirmationDialog,
                title = { Text(stringResource(R.string.confirm_deletion)) },
                text = { Text(stringResource(R.string.confirm_task_deletion_message)) },
                confirmButton = {
                    TextButton(onClick = onDeleteConfirmed) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteConfirmationDialog) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun BodyContent(
    isEditMode: Boolean,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    @StringRes taskNameInputErrorMessage: Int?,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    @StringRes minutesGoalInputErrorMessage: Int?,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .padding(8.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val taskNameInputIsError = taskNameInputErrorMessage != null
            val minutesGoalInputIsError = minutesGoalInputErrorMessage != null
            OutlinedTextField(
                value = taskNameInput.orEmpty(),
                onValueChange = onTaskNameInputChanged,
                label = { Text(stringResource(R.string.task_name)) },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                isError = taskNameInputIsError
            )
            if (taskNameInputIsError) {
                val errorMessage = taskNameInputErrorMessage?.let { stringResource(it) } ?: ""
                Text(errorMessage, color = MaterialTheme.colors.error)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = minutesGoalInput.orEmpty(),
                onValueChange = onMinutesGoalInputChanged,
                label = { Text(stringResource(R.string.minutes_per_day)) },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = minutesGoalInputIsError
            )
            if (minutesGoalInputIsError) {
                val errorMessage = minutesGoalInputErrorMessage?.let { stringResource(it) } ?: ""
                Text(errorMessage, color = MaterialTheme.colors.error)
            }
        }
        if (isEditMode) {
            Column {
                OutlinedButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = stringResource(R.string.reset_day),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.reset_day))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = stringResource(R.string.archive_task),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.archive_task))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_task),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.delete_task))
                }
            }
        }
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
            isEditMode = true,
            taskNameInput = null,
            onTaskNameInputChanged = {},
            taskNameInputErrorMessage = null,
            minutesGoalInput = "10",
            onMinutesGoalInputChanged = {},
            minutesGoalInputErrorMessage = null,
            onSaveClicked = {},
            onDeleteClicked = {},
            onNavigateUpClick = {},
            onDismissDeleteConfirmationDialog = {},
            onDeleteConfirmed = {},
            showDeleteConfirmationDialog = true
        )
    }
}