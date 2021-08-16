package com.codinginflow.just10minutes2.tasklist.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.containsDate
import com.codinginflow.just10minutes2.common.data.entities.toLocalizedString
import com.codinginflow.just10minutes2.common.ui.composables.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.theme.Dimens
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    addNewTask: () -> Unit,
    editTask: (taskId: Long) -> Unit,
    addEditResult: AddEditTaskViewModel.AddEditTaskResult?,
    onAddEditResultProcessed: () -> Unit,
    navigateToTaskStatistics: (taskId: Long) -> Unit,
    navigateToArchive: () -> Unit,
    navigateToTimer: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState(emptyList())
    val runningTaskId by viewModel.runningTask.collectAsState(null)

    val activeDay by viewModel.activeDay.collectAsState(null)

    val lazyListState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val showStartTimerForNewTaskConfirmationDialog
            by viewModel.showStartTimerForNewTaskConfirmationDialog.observeAsState(false)

    LaunchedEffect(addEditResult) {
        if (addEditResult != null) {
            viewModel.onAddEditResult(addEditResult)
            onAddEditResultProcessed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TaskListViewModel.Event.AddNewTask ->
                    addNewTask()
                is TaskListViewModel.Event.EditTask ->
                    editTask(event.taskId)
                is TaskListViewModel.Event.ShowAddEditResultMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(event.msg))
                is TaskListViewModel.Event.OpenTaskStatistics ->
                    navigateToTaskStatistics(event.taskId)
                is TaskListViewModel.Event.NavigateToArchive ->
                    navigateToArchive()
                TaskListViewModel.Event.NavigateToTimer ->
                    navigateToTimer()
            }
        }
    }

    TaskListBody(
        tasks = tasks,
        runningTaskId = runningTaskId,
        activeDay = activeDay,
        onAddNewTaskClicked = viewModel::onAddNewTaskClicked,
        onEditTaskClicked = viewModel::onEditTaskClicked,
        onOpenTaskStatisticsClicked = viewModel::onOpenTaskStatisticsClicked,
        onNavigateToArchiveClicked = viewModel::onNavigateToArchiveClicked,
        onStartTimerClicked = viewModel::onStartTimerClicked,
        showStartTimerForNewTaskConfirmationDialog = showStartTimerForNewTaskConfirmationDialog,
        onDismissStartTimerForNewTaskConfirmationDialog = viewModel::onDismissStartTimerForNewTaskConfirmationDialog,
        onStartTimerForNewTaskConfirmed = viewModel::onStartTimerForNewTaskConfirmed,
        onStopTimerClicked = viewModel::onStopTimerClicked,
        lazyListState = lazyListState,
        scaffoldState = scaffoldState
    )
}

@Composable
private fun TaskListBody(
    tasks: List<Task>,
    runningTaskId: Long?,
    activeDay: Calendar?,
    onAddNewTaskClicked: () -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    onNavigateToArchiveClicked: () -> Unit,
    onStartTimerClicked: (Task) -> Unit,
    showStartTimerForNewTaskConfirmationDialog: Boolean,
    onDismissStartTimerForNewTaskConfirmationDialog: () -> Unit,
    onStartTimerForNewTaskConfirmed: () -> Unit,
    onStopTimerClicked: () -> Unit, lazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks)) },
                actions = {
                    IconButton(onClick = onNavigateToArchiveClicked) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = stringResource(R.string.show_archived_tasks)
                        )
                    }
                    IconButton(onClick = onAddNewTaskClicked) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_new_task)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            tasks = tasks,
            runningTaskId = runningTaskId,
            activeDay = activeDay,
            onEditTaskClicked = onEditTaskClicked,
            onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
            onStartTimerClicked = onStartTimerClicked,
            onStopTimerClicked = onStopTimerClicked,
            lazyListState = lazyListState,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showStartTimerForNewTaskConfirmationDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.switch_task)) },
            text = { Text(stringResource(R.string.confirm_switch_task_message)) },
            confirmButton = {
                TextButton(onClick = onStartTimerForNewTaskConfirmed) {
                    Text(stringResource(R.string.confirm_switch))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissStartTimerForNewTaskConfirmationDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = onDismissStartTimerForNewTaskConfirmationDialog,
        )
    }
}

@Composable
private fun BodyContent(
    tasks: List<Task>,
    runningTaskId: Long?,
    activeDay: Calendar?,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    onStartTimerClicked: (Task) -> Unit,
    onStopTimerClicked: () -> Unit, lazyListState: LazyListState,
    modifier: Modifier = Modifier

) {
    TaskList(
        tasks = tasks,
        runningTaskId = runningTaskId,
        activeDay = activeDay,
        onEditTaskClicked = onEditTaskClicked,
        onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
        onStartTimerClicked = onStartTimerClicked,
        onStopTimerClicked = onStopTimerClicked,
        lazyListState = lazyListState,
        modifier = modifier
    )
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    runningTaskId: Long?,
    activeDay: Calendar?,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    onStartTimerClicked: (Task) -> Unit,
    onStopTimerClicked: () -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    var expandedItemId by rememberSaveable { mutableStateOf(-1L) }

    // TODO: 15.08.2021 Implement empty views for all screens
    // TODO: 15.08.2021 Implement congratulations message when all tasks are done

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Dimens.ListBottomPadding),
        modifier = modifier
    ) {
        items(tasks) { task ->
            val isActiveToday = activeDay != null && task.weekdays.containsDate(activeDay)
            TaskItem(
                task = task,
                isActiveToday = isActiveToday,
                timerRunning = task.id == runningTaskId,
                expanded = expandedItemId == task.id,
                onTaskClicked = { clickedTask ->
                    expandedItemId = if (expandedItemId == clickedTask.id) {
                        -1L
                    } else {
                        clickedTask.id
                    }
                },
                onEditTaskClicked = onEditTaskClicked,
                onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
                onStartTimerClicked = onStartTimerClicked,
                onStopTimerClicked = onStopTimerClicked
            )
            Divider()
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    isActiveToday: Boolean,
    timerRunning: Boolean,
    expanded: Boolean,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    onStartTimerClicked: (Task) -> Unit,
    onStopTimerClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClicked(task) }
            .animateContentSize()
            .padding(8.dp)
    ) {
        // TODO: 16.08.2021 Change the appearance on inactive days

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.weight(0.85f)
                ) {
                    val taskNameTextColor =
                        when {
                            !isActiveToday -> Color.Gray
                            timerRunning -> MaterialTheme.colors.primary
                            else -> LocalContentColor.current
                        }
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.h6,
                        color = taskNameTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActiveToday) {
                        Text(
                            text = stringResource(
                                R.string.completed_and_total_minutes,
                                task.timeCompletedTodayInMinutes,
                                task.dailyGoalInMinutes
                            ) + " " + stringResource(R.string.completed_today_lowercase),
                            fontSize = 14.sp,
                        )
                    }
                    Text(
                        text = stringResource(R.string.active_on) + ": " + task.weekdays.toLocalizedString(
                            LocalContext.current
                        ),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    if (timerRunning) {
                        Text(
                            stringResource(R.string.timer_running),
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.15f)
                        .padding(8.dp)
                ) {
                    if (isActiveToday) {
                        val progress =
                            task.timeCompletedTodayInMilliseconds.toFloat() / task.dailyGoalInMilliseconds.toFloat()
                        CircularProgressIndicatorWithBackground(
                            progress = progress,
                        )
                        when {
                            task.isCompletedToday -> {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.task_completed),
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                            timerRunning -> {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = stringResource(R.string.timer_running),
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onStopTimerClicked() }
                                )
                            }
                            else -> {
                                // TODO: 16.08.2021 Click target too small -> move to whole progress circle
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.start_timer),
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onStartTimerClicked(task) }
                                )
                            }
                        }
                    }
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedButton(
                        onClick = { onEditTaskClicked(task) },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_task),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.edit_task))
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onOpenTaskStatisticsClicked(task) },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = stringResource(R.string.statistics),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.statistics))
                    }
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
        TaskListBody(
            tasks = listOf(
                Task("Example Task 1", timeCompletedTodayInMilliseconds = (0 * 60 * 1000).toLong()),
                Task("Example Task 2", timeCompletedTodayInMilliseconds = (3 * 60 * 1000).toLong()),
                Task("Example Task 3", timeCompletedTodayInMilliseconds = (8 * 60 * 1000).toLong()),
            ),
            runningTaskId = 1,
            activeDay = Calendar.getInstance(),
            onAddNewTaskClicked = {},
            onEditTaskClicked = {},
            onOpenTaskStatisticsClicked = {},
            onNavigateToArchiveClicked = {},
            onStartTimerClicked = {},
            onStopTimerClicked = {},
            showStartTimerForNewTaskConfirmationDialog = false,
            onDismissStartTimerForNewTaskConfirmationDialog = {},
            onStartTimerForNewTaskConfirmed = {},
            lazyListState = rememberLazyListState(),
            scaffoldState = rememberScaffoldState()
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
private fun PreviewTaskItem() {
    Just10Minutes2Theme {
        Surface {
            TaskItem(
                task = Task(
                    "Example Task",
                    timeCompletedTodayInMilliseconds = (3 * 60 * 1000).toLong()
                ),
                isActiveToday = true,
                timerRunning = true,
                expanded = true,
                onTaskClicked = {},
                onEditTaskClicked = {},
                onOpenTaskStatisticsClicked = {},
                onStartTimerClicked = {},
                onStopTimerClicked = {}
            )
        }
    }
}