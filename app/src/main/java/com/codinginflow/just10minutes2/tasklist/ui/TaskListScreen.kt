package com.codinginflow.just10minutes2.tasklist.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState(emptyList())

    TaskListBody(tasks = tasks)
}

@Composable
private fun TaskListBody(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_task_list)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
                Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_new_task))
            }
        }
    ) { innerPadding ->
        TaskList(tasks = tasks, modifier = Modifier.padding(innerPadding))
    }

}

@Composable
private fun TaskList(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val expandedItemIds = rememberSaveable(
        key = "expandedItemIds", // explicit key necessary because of bug in navigation compose
        saver = listSaver(
            save = { it },
            restore = {
                mutableStateListOf<Long>().apply { addAll(it) }
            }
        )
    ) { mutableStateListOf<Long>() }

    LazyColumn(modifier) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                expanded = expandedItemIds.contains(task.id),
                onTaskClicked = { clickedTask ->
                    if (expandedItemIds.contains(clickedTask.id)) {
                        expandedItemIds.remove(clickedTask.id)
                    } else {
                        expandedItemIds.add(clickedTask.id)
                    }
                }
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
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedButton(
                        onClick = { /*TODO*/ },
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
                        onClick = { /*TODO*/ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.start_timer),
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.start_timer))
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
            )
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
            onTaskClicked = {}
        )
    }
}