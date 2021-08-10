package com.codinginflow.just10minutes2.tasklist.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    addNewTask: () -> Unit,
    editTask: (taskId: Long) -> Unit,
) {
    val tasks by viewModel.tasks.collectAsState(emptyList())
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TaskListViewModel.Event.AddNewTask ->
                    addNewTask()
                is TaskListViewModel.Event.EditTask ->
                    editTask(event.taskId)
            }
        }
    }

    TaskListBody(
        tasks = tasks,
        onAddNewTaskClicked = viewModel::onAddNewTaskClicked,
        onEditTaskClicked = viewModel::onEditTaskClicked,
        lazyListState = lazyListState
    )
}

@Composable
private fun TaskListBody(
    tasks: List<Task>,
    onAddNewTaskClicked: () -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_task_list)) },
                actions = {
                    IconButton(onClick = onAddNewTaskClicked) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_new_task)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            tasks = tasks,
            onEditTaskClicked = onEditTaskClicked,
            lazyListState = lazyListState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    tasks: List<Task>,
    onEditTaskClicked: (Task) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier

) {
    TaskList(
        tasks = tasks,
        onEditTaskClicked = onEditTaskClicked,
        lazyListState = lazyListState,
        modifier = modifier,
    )
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onEditTaskClicked: (Task) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    var expandedItemId by rememberSaveable { mutableStateOf(-1L) }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 50.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                expanded = expandedItemId == task.id,
                onTaskClicked = { clickedTask ->
                    expandedItemId = if (expandedItemId == clickedTask.id) {
                        -1L
                    } else {
                        clickedTask.id
                    }
                },
                onEditTaskClicked = onEditTaskClicked
            )
            Divider()
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    expanded: Boolean,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
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
                Column(Modifier.weight(0.8f)) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            R.string.daily_minutes_goal_placeholder,
                            task.dailyGoalInMinutes
                        ),
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(
                            R.string.minutes_completed_today_placeholder,
                            task.minutesCompletedToday
                        ),
                        color = MaterialTheme.colors.primary
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(16.dp)
                ) {
                    val progress =
                        1 - (task.millisLeftToday.toFloat() / task.dailyGoalInMillis.toFloat())
                    CircularProgressIndicatorWithBackground(
                        progress = progress,
                    )
                    if (task.isCompletedToday) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.task_completed),
                            tint = MaterialTheme.colors.primary
                        )
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
                    val timerButtonTextRes =
                        if (!task.isCompletedToday) R.string.open_timer else R.string.task_completed
                    OutlinedButton(
                        onClick = { /*TODO*/ },
                        enabled = !task.isCompletedToday,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.start_timer),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(timerButtonTextRes))
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
                Task("Example Task 1"),
                Task("Example Task 2"),
                Task("Example Task 3"),
            ),
            onAddNewTaskClicked = {},
            onEditTaskClicked = {},
            lazyListState = rememberLazyListState()
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
        TaskItem(
            task = Task("Example Task"),
            expanded = true,
            onTaskClicked = {},
            onEditTaskClicked = {}
        )
    }
}