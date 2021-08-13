package com.codinginflow.just10minutes2.tasklist.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.addedittask.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.SharedViewModel
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.exp

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
    addNewTask: () -> Unit,
    editTask: (taskId: Long) -> Unit,
    addEditResult: AddEditTaskViewModel.AddEditTaskResult?,
    onAddEditResultProcessed: () -> Unit,
    navigateToTimer: () -> Unit
) {
    val tasks by viewModel.tasks.observeAsState(emptyList())
    val runningTaskId by viewModel.runningTask.collectAsState(null)
    val archiveViewActive by viewModel.archiveViewActive.observeAsState()
    val expandedItemId by viewModel.expandedItemId.observeAsState()

    val lazyListStateNonArchived = rememberLazyListState()
    val lazyListStateArchived = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

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
                is TaskListViewModel.Event.ShowAddEditScreenConfirmationMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(event.msg))
                is TaskListViewModel.Event.OpenTimerForTask -> {
                    sharedViewModel.setTaskIdToOpenInTimer(event.task)
                    navigateToTimer()
                }
            }
        }
    }

    TaskListBody(
        tasks = tasks,
        runningTaskId = runningTaskId,
        expandedItemId = expandedItemId,
        onTaskClicked = viewModel::onTaskClicked,
        onAddNewTaskClicked = viewModel::onAddNewTaskClicked,
        onOpenTimerForTaskClicked = viewModel::onOpenTimerForTaskClicked,
        onEditTaskClicked = viewModel::onEditTaskClicked,
        archiveViewActive = archiveViewActive,
        onToggleArchiveViewClicked = viewModel::onToggleArchiveViewClicked,
        lazyListStateNonArchived = lazyListStateNonArchived,
        lazyListStateArchived = lazyListStateArchived,
        scaffoldState = scaffoldState
    )
}

@Composable
private fun TaskListBody(
    tasks: List<Task>,
    runningTaskId: Long?,
    expandedItemId: Long?,
    onTaskClicked: (Task) -> Unit,
    onAddNewTaskClicked: () -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTimerForTaskClicked: (Task) -> Unit,
    archiveViewActive: Boolean?,
    onToggleArchiveViewClicked: () -> Unit,
    lazyListStateNonArchived: LazyListState,
    lazyListStateArchived: LazyListState,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks)) },
                actions = {
                    if (archiveViewActive != null) { // nullable boolean to avoid seeing the wrong icon for a moment
                        if (!archiveViewActive) {
                            IconButton(onClick = onAddNewTaskClicked) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_new_task)
                                )
                            }
                        }
                        val toggleArchiveIcon =
                            if (archiveViewActive) Icons.Default.List else Icons.Default.Inventory2
                        IconButton(onClick = onToggleArchiveViewClicked) {
                            Icon(
                                imageVector = toggleArchiveIcon,
                                contentDescription = stringResource(R.string.show_archived_tasks)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            tasks = tasks,
            runningTaskId = runningTaskId,
            expandedItemId = expandedItemId,
            onTaskClicked = onTaskClicked,
            onEditTaskClicked = onEditTaskClicked,
            onOpenTimerForTaskClicked = onOpenTimerForTaskClicked,
            archiveViewActive = archiveViewActive,
            lazyListStateNonArchived = lazyListStateNonArchived,
            lazyListStateArchived = lazyListStateArchived,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    tasks: List<Task>,
    runningTaskId: Long?,
    expandedItemId: Long?,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTimerForTaskClicked: (Task) -> Unit,
    archiveViewActive: Boolean?,
    lazyListStateNonArchived: LazyListState,
    lazyListStateArchived: LazyListState,
    modifier: Modifier = Modifier

) {
    TaskList(
        tasks = tasks,
        runningTaskId = runningTaskId,
        expandedItemId = expandedItemId,
        onTaskClicked = onTaskClicked,
        onEditTaskClicked = onEditTaskClicked,
        onOpenTimerForTaskClicked = onOpenTimerForTaskClicked,
        archiveViewActive = archiveViewActive,
        lazyListStateNonArchived = lazyListStateNonArchived,
        lazyListStateArchived = lazyListStateArchived,
        modifier = modifier
    )
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    runningTaskId: Long?,
    expandedItemId: Long?,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTimerForTaskClicked: (Task) -> Unit,
    archiveViewActive: Boolean?,
    lazyListStateNonArchived: LazyListState,
    lazyListStateArchived: LazyListState,
    modifier: Modifier = Modifier
) {
    if (archiveViewActive == false) {
        LazyColumn( // non-archived tasks
            state = lazyListStateNonArchived,
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 50.dp)
        ) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    timerRunning = task.id == runningTaskId,
                    expanded = expandedItemId == task.id,
                    onTaskClicked = onTaskClicked,
                    onEditTaskClicked = onEditTaskClicked,
                    onOpenTimerForTaskClicked = onOpenTimerForTaskClicked
                )
                Divider()
            }
        }
    } else {
        Column(modifier = modifier) {
            LazyColumn( // archived tasks
                state = lazyListStateArchived,
                contentPadding = PaddingValues(bottom = 50.dp)
            ) {
                item {
                    Text(stringResource(R.string.archive_explanation), Modifier.padding(8.dp))
                }
                items(tasks) { task ->
                    TaskItemArchived(
                        task = task,
                        expanded = expandedItemId == task.id,
                        onTaskClicked = onTaskClicked,
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    timerRunning: Boolean,
    expanded: Boolean,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTimerForTaskClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClicked(task) }
            .animateContentSize()
            .padding(8.dp)
    ) {
        Column {
            Row {
                Column(
                    Modifier
                        .weight(0.8f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            R.string.completed_and_total_minutes,
                            task.timeCompletedTodayInMinutes,
                            task.dailyGoalInMinutes
                        ) + " " + stringResource(R.string.today),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(8.dp)
                ) {
                    val progress =
                        1 - (task.timeLeftTodayInMilliseconds.toFloat() / task.dailyGoalInMilliseconds.toFloat())
                    CircularProgressIndicatorWithBackground(
                        progress = progress,
                    )
                    if (timerRunning) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val animatedAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 1500
                                    0.6f at 500
                                },
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = stringResource(R.string.timer_running),
                            tint = MaterialTheme.colors.primary.copy(alpha = animatedAlpha)
                        )
                    } else if (task.isCompletedToday) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.task_completed),
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            }
            if (expanded) {
                Column {
                    if (timerRunning) {
                        Text(
                            stringResource(R.string.timer_running),
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.primary
                        )
                    }
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
                            onClick = { onOpenTimerForTaskClicked(task) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = stringResource(R.string.open_timer),
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.open_timer))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedButton(
                            onClick = { },
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
                        Spacer(Modifier.width(8.dp))
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemArchived(
    task: Task,
    expanded: Boolean,
    onTaskClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClicked(task) }
            .animateContentSize()
            .padding(8.dp)
    ) {
        Column {
            Row {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.9f)
                )
                val dropdownIcon =
                    if (!expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
                Icon(
                    dropdownIcon,
                    contentDescription = stringResource(R.string.show_more),
                    modifier = Modifier.weight(0.1f)
                )
            }
            if (expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedButton(
                            onClick = { },
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
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Unarchive,
                                contentDescription = stringResource(R.string.unarchive),
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.unarchive))
                        }
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
                Task("Example Task 1", millisCompletedToday = (0 * 60 * 1000).toLong()),
                Task("Example Task 2", millisCompletedToday = (3 * 60 * 1000).toLong()),
                Task("Example Task 3", millisCompletedToday = (8 * 60 * 1000).toLong()),
            ),
            runningTaskId = 1,
            expandedItemId = 1,
            onTaskClicked = {},
            onAddNewTaskClicked = {},
            onEditTaskClicked = {},
            onOpenTimerForTaskClicked = {},
            archiveViewActive = false,
            onToggleArchiveViewClicked = {},
            lazyListStateNonArchived = rememberLazyListState(),
            lazyListStateArchived = rememberLazyListState()
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
                task = Task("Example Task", millisCompletedToday = (3 * 60 * 1000).toLong()),
                timerRunning = true,
                expanded = true,
                onTaskClicked = {},
                onEditTaskClicked = {},
                onOpenTimerForTaskClicked = {},
            )
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
private fun PreviewTaskItemArchived() {
    Just10Minutes2Theme {
        Surface {
            TaskItemArchived(
                task = Task("Example Task", millisCompletedToday = (3 * 60 * 1000).toLong()),
                expanded = true,
                onTaskClicked = {},
            )
        }
    }
}