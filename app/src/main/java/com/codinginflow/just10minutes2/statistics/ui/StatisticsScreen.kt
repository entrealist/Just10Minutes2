package com.codinginflow.just10minutes2.statistics.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.theme.Dimens
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest
import java.text.DateFormat
import java.util.*

// TODO: 17.08.2021 Implement UiState here to avoid flickering

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    navigateToTaskStatistics: (taskId: Long) -> Unit,
) {
    val notArchivedTasks by viewModel.notArchivedTasks.collectAsState(emptyList())
    val archivedTasks by viewModel.archivedTasks.collectAsState(emptyList())
    val taskStatistics by viewModel.taskStatistics.collectAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StatisticsViewModel.Event.OpenTaskStatistics ->
                    navigateToTaskStatistics(event.taskId)
            }
        }
    }

    StatisticsBody(
        notArchivedTasks = notArchivedTasks,
        archivedTasks = archivedTasks,
        taskStatistics = taskStatistics,
        onTaskDetailsClicked = viewModel::onTaskDetailsClicked,
    )
}

@Composable
private fun StatisticsBody(
    notArchivedTasks: List<Task>,
    archivedTasks: List<Task>,
    taskStatistics: List<TaskStatistic>,
    onTaskDetailsClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
            )
        }
    ) { innerPadding ->
        BodyContent(
            notArchivedTasks = notArchivedTasks,
            archivedTasks = archivedTasks,
            taskStatistics = taskStatistics,
            onTaskDetailsClicked = onTaskDetailsClicked,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    notArchivedTasks: List<Task>,
    archivedTasks: List<Task>,
    taskStatistics: List<TaskStatistic>,
    onTaskDetailsClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    TaskWithStatisticsList(
        notArchivedTasks = notArchivedTasks,
        archivedTasks = archivedTasks,
        taskStatistics = taskStatistics,
        onTaskDetailsClicked = onTaskDetailsClicked,
        modifier = modifier
    )
}

@Composable
private fun TaskWithStatisticsList(
    notArchivedTasks: List<Task>,
    archivedTasks: List<Task>,
    taskStatistics: List<TaskStatistic>,
    onTaskDetailsClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dimens.ListBottomPadding),
        modifier = modifier
    ) {
        item {
            Row(modifier = Modifier.padding(8.dp)) {
                Icon(
                    Icons.Default.List,
                    contentDescription = stringResource(R.string.active_tasks),
                    Modifier.align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.active_tasks),
                    style = MaterialTheme.typography.h6,
                )
            }
            Divider()
        }
        items(notArchivedTasks) { task ->
            val statisticsForTask = remember(taskStatistics) {
                // TODO: 15.08.2021 Put this filtering logic into the ViewModel
                taskStatistics.filter { it.taskId == task.id }
            }
            TaskWithStatisticsItem(
                task = task,
                statistics = statisticsForTask,
                onTaskDetailsClicked = onTaskDetailsClicked
            )
            Divider()
        }
        item {
                Spacer(Modifier.height(32.dp))
            Row(modifier = Modifier.padding(8.dp)) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = stringResource(R.string.archived_tasks),
                    Modifier.align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.archived_tasks),
                    style = MaterialTheme.typography.h6,
                )
            }
            Divider()
        }
        items(archivedTasks) { task ->
            val statisticsForTask = remember(taskStatistics) {
                // TODO: 15.08.2021 Put this filtering logic into the ViewModel
                taskStatistics.filter { it.taskId == task.id }
            }
            TaskWithStatisticsItem(
                task = task,
                statistics = statisticsForTask,
                onTaskDetailsClicked = onTaskDetailsClicked
            )
            Divider()
        }
    }
}

@Composable
private fun TaskWithStatisticsItem(
    task: Task,
    statistics: List<TaskStatistic>,
    onTaskDetailsClicked: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = statistics.isNotEmpty()) { onTaskDetailsClicked(task) }
                .padding(8.dp)
        ) {
            Text(
                text = task.name,
                style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (statistics.isNotEmpty()) {
                Row {
                    Text(stringResource(R.string.details), color = Color.Gray)
                    Icon(
                        Icons.Default.ArrowRight,
                        contentDescription = stringResource(R.string.details),
                        tint = Color.Gray
                    )
                }
            }
        }
        if (statistics.isEmpty()) {
            Text(
                stringResource(R.string.no_statistics_for_task_message),
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = Color.Gray
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statistics) { statistic ->
                    StatisticItem(statistic)
                    Spacer(Modifier.width(4.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatisticItem(
    statistic: TaskStatistic,
    modifier: Modifier = Modifier
) {
    val date = Date()
    date.time = statistic.dayTimestamp
    val dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(date)

    Card(elevation = 4.dp, modifier = modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(dateString, Modifier.align(Alignment.CenterHorizontally))
            val icon = if (statistic.taskCompleted) Icons.Default.Check else Icons.Default.Close
            val iconTint = if (statistic.taskCompleted) MaterialTheme.colors.primary else Color.Gray
            val contentDescription =
                if (statistic.taskCompleted) R.string.task_completed else R.string.task_not_completed
            Icon(
                icon,
                stringResource(contentDescription),
                modifier = Modifier
                    .padding(4.dp)
                    .size(32.dp)
                    .align(Alignment.CenterHorizontally),
                tint = iconTint
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
private fun PreviewStatisticsScreen() {
    Just10Minutes2Theme {
        StatisticsBody(
            notArchivedTasks = listOf(
                Task("Example task 1 mmmmmmmmmmmmmmmmmmmmmmmmmmm", id = 1),
                Task("Example task 2", id = 2),
                Task("Example task 3", id = 3),
                Task("Example task 4", id = 4),
            ),
            archivedTasks = listOf(
                Task("Example task 5 mmmmmmmmmmmmmmmmmmmmmmmmmmm", id = 1, archived = true),
                Task("Example task 6", id = 2, archived = true),
            ),
            taskStatistics = listOf(
                TaskStatistic(1, 1628892000000, 10, 10 * 60_000L),
                TaskStatistic(1, 1628805600000, 10, 8 * 60_000L),
                TaskStatistic(1, 1628719200000, 15, 10 * 60_000L),
                TaskStatistic(2, 1628892000000, 10, 10 * 60_000L),
                TaskStatistic(2, 1628805600000, 10, 8 * 60_000L),
                TaskStatistic(2, 1628719200000, 15, 10 * 60_000L),
                TaskStatistic(3, 1628892000000, 10, 10 * 60_000L),
                TaskStatistic(3, 1628805600000, 10, 8 * 60_000L),
                TaskStatistic(3, 1628719200000, 15, 10 * 60_000L),
            ),
            onTaskDetailsClicked = {},
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
private fun PreviewTaskWithStatisticsItem() {
    Just10Minutes2Theme {
        Surface {
            TaskWithStatisticsItem(
                task = Task("Example task mmmmmmmmmmmmmmmmmmmmmmmmmmmm", id = 1),
                statistics = listOf(
                    TaskStatistic(1, 1628892000000, 10, 10 * 60_000L),
                    TaskStatistic(1, 1628805600000, 10, 8 * 60_000L),
                    TaskStatistic(1, 1628719200000, 15, 10 * 60_000L),
                    TaskStatistic(1, 1628632800000, 15, 17 * 60_000L),
                    TaskStatistic(1, 1628546400000, 15, 15 * 60_000L),
                    TaskStatistic(1, 1628460000000, 20, 18 * 60_000L),
                ),
                onTaskDetailsClicked = {}
            )
        }
    }
}