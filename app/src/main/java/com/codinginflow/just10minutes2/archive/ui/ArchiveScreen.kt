package com.codinginflow.just10minutes2.archive

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
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.archive.ui.ArchiveViewModel
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.theme.Dimens
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel = hiltViewModel(),
    navigateToTaskStatistics: (taskId: Long) -> Unit,
    editTask: (taskId: Long) -> Unit,
    editResult: AddEditTaskViewModel.AddEditTaskResult?,
    onEditResultProcessed: () -> Unit,
    navigateUp: () -> Unit,
) {
    val archivedTasks by viewModel.archivedTasks.collectAsState(emptyList())

    val lazyListState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    LaunchedEffect(editResult) {
        if (editResult != null) {
            viewModel.onEditResult(editResult)
            onEditResultProcessed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ArchiveViewModel.Event.NavigateUp ->
                    navigateUp()
                is ArchiveViewModel.Event.ShowUnarchivedConfirmationMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.task_unarchived))
                is ArchiveViewModel.Event.OpenTaskStatistics ->
                    navigateToTaskStatistics(event.taskId)
                is ArchiveViewModel.Event.EditTask ->
                    editTask(event.taskId)
                is ArchiveViewModel.Event.ShowAddEditResultMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(event.msg))
            }
        }
    }

    ArchiveBody(
        archivedTasks = archivedTasks,
        onNavigateUpClicked = viewModel::onNavigateUpClicked,
        onOpenTaskStatisticsClicked = viewModel::onOpenTaskStatisticsClicked,
        onEditTaskClicked = viewModel::onEditTaskClicked,
        scaffoldState = scaffoldState,
        lazyListState = lazyListState
    )
}

@Composable
private fun ArchiveBody(
    archivedTasks: List<Task>,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    onNavigateUpClicked: () -> Unit,
    lazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUpClicked) {
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
            archivedTasks = archivedTasks,
            modifier = Modifier.padding(innerPadding),
            onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
            onEditTaskClicked = onEditTaskClicked,
            lazyListState = lazyListState
        )
    }
}

@Composable
private fun BodyContent(
    archivedTasks: List<Task>,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    ArchivedTaskList(
        archivedTasks = archivedTasks,
        onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
        onEditTaskClicked = onEditTaskClicked,
        lazyListState = lazyListState,
        modifier = modifier
    )
}

@Composable
private fun ArchivedTaskList(
    archivedTasks: List<Task>,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    var expandedItemId by rememberSaveable { mutableStateOf(-1L) }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Dimens.ListBottomPadding),
        modifier = modifier
    ) {
        item {
            Text(stringResource(R.string.archive_explanation), Modifier.padding(8.dp))
            Divider()
        }
        items(archivedTasks) { task ->
            TaskItemArchived(
                task = task,
                expanded = expandedItemId == task.id,
                onTaskClicked = { clickedTask ->
                    expandedItemId = if (expandedItemId == clickedTask.id) {
                        -1L
                    } else {
                        clickedTask.id
                    }
                },
                onOpenTaskStatisticsClicked = onOpenTaskStatisticsClicked,
                onEditTaskClicked = onEditTaskClicked
            )
            Divider()
        }
    }
}

@Composable
private fun TaskItemArchived(
    task: Task,
    expanded: Boolean,
    onTaskClicked: (Task) -> Unit,
    onEditTaskClicked: (Task) -> Unit,
    onOpenTaskStatisticsClicked: (Task) -> Unit,
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
                        .weight(0.9f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.last_active,
                            "Insert date here", // TODO: 14.08.2021 Insert date
                        ),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                val dropdownIcon =
                    if (!expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
                Icon(
                    dropdownIcon,
                    contentDescription = stringResource(R.string.show_more),
                    modifier = Modifier
                        .weight(0.1f)
                        .align(Alignment.CenterVertically)
                )
            }
            if (expanded) {
                Column {
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
private fun PreviewArchiveScreen() {
    Just10Minutes2Theme {
        ArchiveBody(
            archivedTasks = listOf(
                Task("Example Task 1", timeCompletedTodayInMilliseconds = (0 * 60 * 1000).toLong()),
                Task("Example Task 2", timeCompletedTodayInMilliseconds = (3 * 60 * 1000).toLong()),
                Task("Example Task 3", timeCompletedTodayInMilliseconds = (8 * 60 * 1000).toLong()),
            ),
            onNavigateUpClicked = {},
            onOpenTaskStatisticsClicked = {},
            onEditTaskClicked = {},
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
private fun PreviewTaskItemArchived() {
    Just10Minutes2Theme {
        Surface {
            TaskItemArchived(
                task = Task(
                    "Example Task",
                    timeCompletedTodayInMilliseconds = (3 * 60 * 1000).toLong()
                ),
                expanded = true,
                onTaskClicked = {},
                onOpenTaskStatisticsClicked = {},
                onEditTaskClicked = {}
            )
        }
    }
}