package com.codinginflow.just10minutes2.timer.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import java.util.*

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val selectedTask by viewModel.selectedTask.collectAsState(null)
    val allTasks by viewModel.allTasks.collectAsState(emptyList())

    val timerRunning by viewModel.timerRunning.collectAsState(false)

    TimerBody(
        selectedTask = selectedTask,
        allTasks = allTasks,
        timerRunning = timerRunning,
        onNewTaskSelected = viewModel::onNewTaskSelected,
        onStartTimerClicked = viewModel::onStartTimerClicked,
        onStopTimerClicked = viewModel::onStopTimerClicked,
    )
}

@Composable
private fun TimerBody(
    selectedTask: Task?,
    allTasks: List<Task>,
    timerRunning: Boolean,
    onNewTaskSelected: (Task) -> Unit,
    onStartTimerClicked: () -> Unit,
    onStopTimerClicked: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_timer)) },
            )
        },
    ) {
        BodyContent(
            selectedTask = selectedTask,
            allTasks = allTasks,
            timerRunning = timerRunning,
            onNewTaskSelected = onNewTaskSelected,
            onStartTimerClicked = onStartTimerClicked,
            onStopTimerClicked = onStopTimerClicked
        )
    }
}

@Composable
private fun BodyContent(
    selectedTask: Task?,
    allTasks: List<Task>,
    timerRunning: Boolean,
    onNewTaskSelected: (Task) -> Unit,
    onStartTimerClicked: () -> Unit,
    onStopTimerClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.active_task_colon), color = Color.Gray)
        DropdownSelector(
            selectedTask = selectedTask,
            allTasks = allTasks,
            onNewTaskSelected = onNewTaskSelected
        )
        CircularTextTimer(
            timeLeftInMillis = selectedTask?.millisLeftToday ?: 0,
            timeGoalInMillis = selectedTask?.dailyGoalInMilliseconds ?: 0
        )
        Spacer(Modifier.height(16.dp))

        val buttonEnabled = selectedTask != null && !selectedTask.isCompletedToday
        val buttonOnClick = if (timerRunning) onStopTimerClicked else onStartTimerClicked
        val buttonTextRes = if (timerRunning) R.string.stop_timer else R.string.start_timer
        Button(
            onClick = buttonOnClick,
            enabled = buttonEnabled
        ) {
            Text(stringResource(buttonTextRes))
        }
    }
}

@Composable
private fun DropdownSelector(
    selectedTask: Task?,
    allTasks: List<Task>,
    onNewTaskSelected: (Task) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = if (selectedTask != null) {
        stringResource(
            R.string.task_name_with_minutes_goal,
            selectedTask.name,
            selectedTask.dailyGoalInMinutes
        )
    } else stringResource(id = R.string.no_task_selected)

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text(
                selectedText,
                style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.primary
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(id = R.string.select_task),
                tint = MaterialTheme.colors.primary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(250.dp)
        ) {
            allTasks.forEach { task ->
                if (task != selectedTask) {
                    DropdownMenuItem(onClick = {
                        onNewTaskSelected(task)
                        expanded = false
                    }) {
                        Text(
                            stringResource(
                                R.string.task_name_with_minutes_goal,
                                task.name,
                                task.dailyGoalInMinutes
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularTextTimer(
    timeLeftInMillis: Long,
    timeGoalInMillis: Long,
    progressBarSize: Dp = 230.dp,
    strokeWidth: Dp = 10.dp
) {
    val active = timeGoalInMillis > 0
    val progress = 1 - (timeLeftInMillis.toFloat() / timeGoalInMillis.toFloat())
    val completed = timeLeftInMillis <= 0

    val millisAdjusted = timeLeftInMillis + 999
    val minutes = ((millisAdjusted / 1000) / 60).toInt()
    val seconds = ((millisAdjusted / 1000) % 60).toInt()
    val timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val timeTextColor = if (!completed) LocalContentColor.current else Color.LightGray

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicatorWithBackground(
            progress = if (active) progress else 0f,
            strokeWidth = strokeWidth,
            modifier = Modifier.size(progressBarSize),
        )
        if (active && completed) {
            Text(
                text = stringResource(R.string.completed),
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.primary
            )
        } else {
            Text(text = timeText, style = MaterialTheme.typography.h3, color = timeTextColor)
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
private fun PreviewTimerScreen() {
    Just10Minutes2Theme {
        TimerBody(
            selectedTask = null,
            allTasks = emptyList(),
            timerRunning = false,
            onNewTaskSelected = {},
            onStartTimerClicked = {},
            onStopTimerClicked = {}
        )
    }
}