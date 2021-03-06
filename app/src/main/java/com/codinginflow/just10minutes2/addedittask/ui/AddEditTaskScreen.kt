package com.codinginflow.just10minutes2.addedittask.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.WeekdaySelection
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
    val weekdaysSelectionInput by viewModel.weekdaysSelectionInput.observeAsState()

    val taskNameInputErrorMessage by viewModel.taskNameInputErrorMessage.observeAsState()
    val minutesGoalInputErrorMessage by viewModel.minutesGoalInputErrorMessage.observeAsState()

    val isEditMode = viewModel.isEditMode
    val isArchivedTask by viewModel.isArchivedTask.collectAsState(null)

    val showDeleteTaskConfirmationDialog by
    viewModel.showDeleteTaskConfirmationDialog.observeAsState(false)

    val showResetDayConfirmationDialog by
    viewModel.showResetDayConfirmationDialog.observeAsState(false)

    val showArchiveTaskConfirmationDialog by
    viewModel.showArchiveTaskConfirmationDialog.observeAsState(false)

    val showUnarchiveTaskConfirmationDialog by
    viewModel.showUnarchiveTaskConfirmationDialog.observeAsState(false)

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateBackWithResult ->
                    navigateBackWithResult(event.result)
                is AddEditTaskViewModel.Event.NavigateUp -> navigateUp()
                is AddEditTaskViewModel.Event.ShowResetDayCompletedMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.task_day_reset_completed))
                AddEditTaskViewModel.Event.ShowArchiveTaskCompletedMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.archive_task_completed))
                AddEditTaskViewModel.Event.ShowUnarchiveTaskCompletedMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.unarchive_task_completed))
            }
        }
    }

    AddEditTaskBody(
        isEditMode = isEditMode,
        isArchivedTask = isArchivedTask,
        taskNameInput = taskNameInput,
        onTaskNameInputChanged = viewModel::onTaskNameInputChanged,
        taskNameInputErrorMessage = taskNameInputErrorMessage,
        minutesGoalInput = minutesGoalInput,
        onMinutesGoalInputChanged = viewModel::onMinutesGoalInputChanged,
        minutesGoalInputErrorMessage = minutesGoalInputErrorMessage,
        weekdaysSelectionInput = weekdaysSelectionInput,
        onWeekdaySelectionChanged = viewModel::onWeekdaysSelectionInputChanged,
        onNavigateUpClicked = viewModel::onNavigateUpClicked,
        onSaveClicked = viewModel::onSaveClicked,
        onResetDayClicked = viewModel::onResetDayClicked,
        showResetDayConfirmationDialog = showResetDayConfirmationDialog,
        onDismissResetDayConfirmationDialog = viewModel::onDismissResetDayConfirmationDialog,
        onResetDayConfirmed = viewModel::onResetDayConfirmed,
        onArchiveTaskClicked = viewModel::onArchiveTaskClicked,
        showArchiveTaskConfirmationDialog = showArchiveTaskConfirmationDialog,
        onDismissArchiveTaskConfirmationDialog = viewModel::onDismissArchiveTaskConfirmationDialog,
        onArchiveTaskConfirmed = viewModel::onArchiveTaskConfirmed,
        onUnarchiveTaskClicked = viewModel::onUnarchiveTaskClicked,
        showUnarchiveTaskConfirmationDialog = showUnarchiveTaskConfirmationDialog,
        onDismissUnarchiveTaskConfirmationDialog = viewModel::onDismissUnarchiveTaskConfirmationDialog,
        onUnarchiveTaskConfirmed = viewModel::onUnarchiveTaskConfirmed,
        onDeleteTaskClicked = viewModel::onDeleteTaskClicked,
        showDeleteTaskConfirmationDialog = showDeleteTaskConfirmationDialog,
        onDismissDeleteTaskConfirmationDialog = viewModel::onDismissDeleteTaskConfirmationDialog,
        onDeleteTaskConfirmed = viewModel::onDeleteTaskConfirmed,
        scaffoldState = scaffoldState
    )
}

@Composable
private fun AddEditTaskBody(
    isEditMode: Boolean,
    isArchivedTask: Boolean?,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    @StringRes taskNameInputErrorMessage: Int?,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    @StringRes minutesGoalInputErrorMessage: Int?,
    weekdaysSelectionInput: WeekdaySelection?,
    onWeekdaySelectionChanged: (WeekdaySelection) -> Unit,
    onNavigateUpClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onResetDayClicked: () -> Unit,
    showResetDayConfirmationDialog: Boolean,
    onDismissResetDayConfirmationDialog: () -> Unit,
    onResetDayConfirmed: () -> Unit,
    onArchiveTaskClicked: () -> Unit,
    showArchiveTaskConfirmationDialog: Boolean,
    onDismissArchiveTaskConfirmationDialog: () -> Unit,
    onArchiveTaskConfirmed: () -> Unit,
    onUnarchiveTaskClicked: () -> Unit,
    showUnarchiveTaskConfirmationDialog: Boolean,
    onDismissUnarchiveTaskConfirmationDialog: () -> Unit,
    onUnarchiveTaskConfirmed: () -> Unit,
    onDeleteTaskClicked: () -> Unit,
    showDeleteTaskConfirmationDialog: Boolean,
    onDismissDeleteTaskConfirmationDialog: () -> Unit,
    onDeleteTaskConfirmed: () -> Unit,
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
                    IconButton(onClick = onNavigateUpClicked) {
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
            isArchivedTask = isArchivedTask,
            taskNameInput = taskNameInput,
            onTaskNameInputChanged = onTaskNameInputChanged,
            taskNameInputErrorMessage = taskNameInputErrorMessage,
            minutesGoalInput = minutesGoalInput,
            onMinutesGoalInputChanged = onMinutesGoalInputChanged,
            minutesGoalInputErrorMessage = minutesGoalInputErrorMessage,
            weekdaysSelectionInput = weekdaysSelectionInput,
            onWeekdaySelectionChanged = onWeekdaySelectionChanged,
            onResetDayClicked = onResetDayClicked,
            onArchiveTaskClicked = onArchiveTaskClicked,
            onUnarchiveTaskClicked = onUnarchiveTaskClicked,
            onDeleteTaskClicked = onDeleteTaskClicked,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showResetDayConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissResetDayConfirmationDialog,
            title = { Text(stringResource(R.string.confirm_reset)) },
            text = { Text(stringResource(R.string.confirm_task_reset_day_message)) },
            confirmButton = {
                TextButton(onClick = onResetDayConfirmed) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissResetDayConfirmationDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showArchiveTaskConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissArchiveTaskConfirmationDialog,
            title = { Text(stringResource(R.string.confirm_archiving)) },
            text = { Text(stringResource(R.string.confirm_archiving_task_message)) },
            confirmButton = {
                TextButton(onClick = onArchiveTaskConfirmed) {
                    Text(stringResource(R.string.archive))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissArchiveTaskConfirmationDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showUnarchiveTaskConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnarchiveTaskConfirmationDialog,
            title = { Text(stringResource(R.string.confirm_unarchiving)) },
            text = { Text(stringResource(R.string.confirm_unarchiving_task_message)) },
            confirmButton = {
                TextButton(onClick = onUnarchiveTaskConfirmed) {
                    Text(stringResource(R.string.unarchive))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUnarchiveTaskConfirmationDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showDeleteTaskConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteTaskConfirmationDialog,
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(stringResource(R.string.confirm_delete_task_message)) },
            confirmButton = {
                TextButton(onClick = onDeleteTaskConfirmed) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteTaskConfirmationDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun BodyContent(
    isEditMode: Boolean,
    isArchivedTask: Boolean?,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    @StringRes taskNameInputErrorMessage: Int?,
    minutesGoalInput: String?,
    onMinutesGoalInputChanged: (String) -> Unit,
    @StringRes minutesGoalInputErrorMessage: Int?,
    weekdaysSelectionInput: WeekdaySelection?,
    onWeekdaySelectionChanged: (WeekdaySelection) -> Unit, onResetDayClicked: () -> Unit,
    onArchiveTaskClicked: () -> Unit,
    onUnarchiveTaskClicked: () -> Unit,
    onDeleteTaskClicked: () -> Unit,
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
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = taskNameInputIsError
            )
            if (taskNameInputIsError) {
                val errorMessage = taskNameInputErrorMessage?.let { stringResource(it) } ?: ""
                Text(errorMessage, color = MaterialTheme.colors.error)
            }
            if (!isEditMode || isArchivedTask == false) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = minutesGoalInput.orEmpty(),
                    onValueChange = onMinutesGoalInputChanged,
                    label = { Text(stringResource(R.string.minutes_per_day)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = minutesGoalInputIsError
                )
                if (minutesGoalInputIsError) {
                    val errorMessage =
                        minutesGoalInputErrorMessage?.let { stringResource(it) } ?: ""
                    Text(errorMessage, color = MaterialTheme.colors.error)
                }
                if (weekdaysSelectionInput != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.active_weekdays) + ":")
                    Spacer(Modifier.height(8.dp))
                    WeekDaySelectorRow(
                        weekdaySelection = weekdaysSelectionInput,
                        onWeekdaySelectionChanged = onWeekdaySelectionChanged
                    )
                }
            }
            if (isArchivedTask == true) {
                Spacer(Modifier.height(8.dp))
                Row {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = stringResource(R.string.archived_tasks),
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.task_is_archived), color = Color.Gray)
                }
            }
        }
        if (isEditMode) {
            Column {
                if (isArchivedTask == true) {
                    OutlinedButton(
                        onClick = onUnarchiveTaskClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Unarchive,
                            contentDescription = stringResource(R.string.unarchive_task),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.unarchive_task))
                    }
                } else if (isArchivedTask == false) {
                    OutlinedButton(
                        onClick = onResetDayClicked,
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
                        onClick = onArchiveTaskClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = stringResource(R.string.archive_task),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.archive_task))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDeleteTaskClicked,
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

@Composable
private fun WeekDaySelectorRow(
    weekdaySelection: WeekdaySelection,
    onWeekdaySelectionChanged: (WeekdaySelection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        WeekDaySelectorItem(
            text = stringResource(R.string.monday_abbrev),
            active = weekdaySelection.mondayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(mondayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.tuesday_abbrev),
            active = weekdaySelection.tuesdayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(tuesdayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.wednesday_abbrev),
            active = weekdaySelection.wednesdayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(wednesdayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.thursday_abbrev),
            active = weekdaySelection.thursdayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(thursdayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.friday_abbrev),
            active = weekdaySelection.fridayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(fridayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.saturday_abbrev),
            active = weekdaySelection.saturdayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(saturdayActive = checked))
            }
        )
        Spacer(Modifier.width(4.dp))
        WeekDaySelectorItem(
            text = stringResource(R.string.sunday_abbrev),
            active = weekdaySelection.sundayActive,
            onActiveStateChanged = { checked ->
                onWeekdaySelectionChanged(weekdaySelection.copy(sundayActive = checked))
            }
        )
    }
}

@Composable
private fun WeekDaySelectorItem(
    text: String,
    active: Boolean,
    onActiveStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (!active) Color.LightGray else MaterialTheme.colors.primary
    val textColor = if (!active) LocalContentColor.current else MaterialTheme.colors.onPrimary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable { onActiveStateChanged(!active) }
    ) {
        Text(
            text = text,
            color = textColor,
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
private fun PreviewAddEditTaskScreen() {
    Just10Minutes2Theme {
        AddEditTaskBody(
            isEditMode = true,
            isArchivedTask = false,
            taskNameInput = null,
            onTaskNameInputChanged = {},
            taskNameInputErrorMessage = null,
            minutesGoalInput = "10",
            onMinutesGoalInputChanged = {},
            minutesGoalInputErrorMessage = null,
            weekdaysSelectionInput = WeekdaySelection(false, true, true, false, true, false, true),
            onWeekdaySelectionChanged = {},
            onSaveClicked = {},
            onNavigateUpClicked = {},
            showResetDayConfirmationDialog = false,
            onResetDayClicked = {},
            onDismissResetDayConfirmationDialog = {},
            onResetDayConfirmed = {},
            showArchiveTaskConfirmationDialog = false,
            onArchiveTaskClicked = {},
            onDismissArchiveTaskConfirmationDialog = {},
            onArchiveTaskConfirmed = {},
            showUnarchiveTaskConfirmationDialog = false,
            onUnarchiveTaskClicked = {},
            onDismissUnarchiveTaskConfirmationDialog = {},
            onUnarchiveTaskConfirmed = {},
            showDeleteTaskConfirmationDialog = false,
            onDeleteTaskClicked = {},
            onDismissDeleteTaskConfirmationDialog = {},
            onDeleteTaskConfirmed = {}
        )
    }
}