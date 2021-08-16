package com.codinginflow.just10minutes2.taskstatistics.ui

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.ui.composables.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.theme.Dimens
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import com.codinginflow.just10minutes2.common.util.formatTimeText
import kotlinx.coroutines.flow.collectLatest
import java.text.DateFormat
import java.util.*

@Composable
fun TaskStatisticsScreen(
    viewModel: TaskStatisticsViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
) {
    val task by viewModel.task.collectAsState(null)
    val taskStatistics by viewModel.taskStatistics.collectAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TaskStatisticsViewModel.Event.NavigateUp -> navigateUp()
            }
        }
    }

    StatisticsDetailBody(
        task = task,
        taskStatistics = taskStatistics,
        onNavigateUpClicked = viewModel::onNavigateUpClicked,
    )
}

@Composable
private fun StatisticsDetailBody(
    task: Task?,
    taskStatistics: List<TaskStatistic>,
    onNavigateUpClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_statistics)) },
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
            task = task,
            taskStatistics = taskStatistics,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    task: Task?,
    taskStatistics: List<TaskStatistic>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            task?.name ?: "",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(8.dp)
        )
        Divider()
        TaskStatisticsList(
            taskStatistics = taskStatistics,
        )
    }
}

@Composable
private fun TaskStatisticsList(
    taskStatistics: List<TaskStatistic>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dimens.ListBottomPadding),
        modifier = modifier
    ) {
        items(taskStatistics) { taskStatistic ->
            StatisticItem(statistic = taskStatistic)
            Divider()
        }
    }
}

@Composable
private fun StatisticItem(
    statistic: TaskStatistic,
    modifier: Modifier = Modifier
) {
    val date = Date()
    date.time = statistic.dayTimestamp
    val dateString = DateFormat.getDateInstance(DateFormat.FULL).format(date)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .weight(0.85f)
        ) {
            Text(dateString)
            Text(stringResource(R.string.goal) + ": " + formatTimeText(statistic.timeGoalInMilliseconds))
            Text(stringResource(R.string.completed) + ": " + formatTimeText(statistic.timeCompletedInMilliseconds))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(0.15f)
                .padding(8.dp)
        ) {
            val progress =
                statistic.timeCompletedInMilliseconds.toFloat() / statistic.timeGoalInMilliseconds.toFloat()
            CircularProgressIndicatorWithBackground(
                progress = progress,
            )
            val icon = if (statistic.taskCompleted) Icons.Default.Check else Icons.Default.Close
            val iconTint =
                if (statistic.taskCompleted) MaterialTheme.colors.primary else Color.Gray
            val contentDescription =
                if (statistic.taskCompleted) R.string.task_completed else R.string.task_not_completed
            Icon(
                icon,
                stringResource(contentDescription),
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
private fun PreviewStatisticsDetailScreen() {
    Just10Minutes2Theme {
        StatisticsDetailBody(
            task = Task("Example task"),
            taskStatistics = listOf(
                TaskStatistic(1, 1628892000000, 10, 10 * 60_000L),
                TaskStatistic(1, 1628805600000, 10, 8 * 60_000L),
                TaskStatistic(1, 1628719200000, 15, 10 * 60_000L),
                TaskStatistic(1, 1628632800000, 15, 17 * 60_000L),
                TaskStatistic(1, 1628546400000, 15, 15 * 60_000L),
                TaskStatistic(1, 1628460000000, 20, 18 * 60_000L)
            ),
            onNavigateUpClicked = {}
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
private fun PreviewStatisticItem() {
    Just10Minutes2Theme {
        Surface {
            StatisticItem(
                statistic = TaskStatistic(1, 1628892000000, 10, 10 * 60_000L)
            )
        }
    }
}