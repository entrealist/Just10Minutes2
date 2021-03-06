package com.codinginflow.just10minutes2.timer.ui

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.application.AddEditTaskResultViewModel
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.WeekdaySelection
import com.codinginflow.just10minutes2.common.data.entities.toLocalizedString
import com.codinginflow.just10minutes2.common.ui.composables.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import com.codinginflow.just10minutes2.common.util.formatTimeText
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    editTask: (taskId: Long) -> Unit,
    addEditTaskResultViewModel: AddEditTaskResultViewModel
) {
    val uiState by viewModel.uiState.collectAsState(null)

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimerViewModel.Event.ShowTimerStoppedMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.timer_stopped))
                is TimerViewModel.Event.EditTask ->
                    editTask(event.taskId)
            }
        }
    }

    LaunchedEffect(Unit) {
        addEditTaskResultViewModel.resultMessage.collectLatest { resultMessageRes ->
            scaffoldState.snackbarHostState.showSnackbar(context.getString(resultMessageRes))
        }
    }

    TimerBody(
        uiState = uiState,
        onStartTimerClicked = viewModel::onStartTimerClicked,
        onStopTimerClicked = viewModel::onStopTimerClicked,
        onTaskSelected = viewModel::onTaskSelected,
        onDismissSelectNewTaskConfirmationDialog = viewModel::onDismissSelectNewTaskConfirmationDialog,
        onSelectNewTaskConfirmed = viewModel::onSelectNewTaskConfirmed,
        onEditTaskClicked = viewModel::onEditTaskClicked,
        scaffoldState = scaffoldState
    )
}

@Composable
private fun TimerBody(
    uiState: TimerUiState?,
    onStartTimerClicked: () -> Unit,
    onStopTimerClicked: () -> Unit,
    onTaskSelected: (Task) -> Unit,
    onDismissSelectNewTaskConfirmationDialog: () -> Unit,
    onSelectNewTaskConfirmed: () -> Unit,
    onEditTaskClicked: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = !menuExpanded }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.open_menu)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            val editItemEnabled = uiState?.selectedTask != null
                            DropdownMenuItem(
                                onClick = onEditTaskClicked,
                                enabled = editItemEnabled
                            ) {
                                Text(stringResource(R.string.edit_task))
                            }
                        }
                    }
                }
            )
        },
    ) {
        if (uiState != null) {
            BodyContent(
                task = uiState.selectedTask,
                allTasks = uiState.allTasks,
                timerRunning = uiState.timerRunning,
                taskActiveToday = uiState.taskActiveToday,
                onTaskSelected = onTaskSelected,
                onStartTimerClicked = onStartTimerClicked,
                onStopTimerClicked = onStopTimerClicked
            )

            if (uiState.showSelectNewTaskConfirmationDialog) {
                AlertDialog(
                    title = { Text(stringResource(R.string.switch_task)) },
                    text = { Text(stringResource(R.string.confirm_switch_task_message)) },
                    confirmButton = {
                        TextButton(onClick = onSelectNewTaskConfirmed) {
                            Text(stringResource(R.string.confirm_switch))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissSelectNewTaskConfirmationDialog) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    onDismissRequest = onDismissSelectNewTaskConfirmationDialog,
                )
            }
        }
    }
}

@Composable
private fun BodyContent(
    task: Task?,
    allTasks: List<Task>,
    taskActiveToday: Boolean,
    timerRunning: Boolean,
    onStartTimerClicked: () -> Unit,
    onStopTimerClicked: () -> Unit,
    onTaskSelected: (Task) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
        //.background(Color.Yellow)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(280.dp)
            //  .background(Color.Green)
        ) {
            DropdownMenuWithSelector(
                selectedTask = task,
                allTasks = allTasks,
                onTaskSelected = onTaskSelected,
            )
            Spacer(Modifier.height(8.dp))
            val dailyGoalText = stringResource(R.string.daily_goal) + ": " +
                    if (task == null) {
                        "-"
                    } else {
                        stringResource(R.string.minutes_placeholder, task.dailyGoalInMinutes)
                    }
            val completedTodayText = stringResource(R.string.completed) + ": " +
                    if (task == null || !taskActiveToday) {
                        "-"
                    } else {
                        stringResource(
                            R.string.minutes_placeholder,
                            task.timeCompletedTodayInMinutes
                        )
                    }
            val activeWeekdaysText = stringResource(R.string.active_on) + ": " +
                    if (task == null) {
                        "-"
                    } else {
                        task.weekdays.toLocalizedString(LocalContext.current)
                    }
            Text(dailyGoalText)
            Text(completedTodayText)
            Text(activeWeekdaysText)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                TaskTimeIndicator(
                    task = task,
                    taskActiveToday = taskActiveToday,
                    running = timerRunning
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
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .size(32.dp)
                            .align(Alignment.BottomCenter),
                        tint = MaterialTheme.colors.primary.copy(alpha = animatedAlpha)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val startStopButtonEnabled = task != null && taskActiveToday && !task.isCompletedToday
            val startStopButtonOnClick =
                if (timerRunning) onStopTimerClicked else onStartTimerClicked
            val startStopButtonIcon =
                if (timerRunning) Icons.Default.Stop else Icons.Default.PlayArrow
            val startStopButtonContentDescriptionRes =
                if (timerRunning) R.string.stop_timer else R.string.start_timer
            val startStopButtonTint =
                if (!taskActiveToday) Color.Gray else MaterialTheme.colors.primary
            IconButton(
                onClick = startStopButtonOnClick,
                enabled = startStopButtonEnabled,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = startStopButtonIcon,
                    contentDescription = stringResource(startStopButtonContentDescriptionRes),
                    tint = startStopButtonTint,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DropdownMenuWithSelector(
    selectedTask: Task?,
    allTasks: List<Task>,
    onTaskSelected: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = selectedTask?.name ?: stringResource(id = R.string.no_task_selected)

    Box(modifier) {
        Box(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                selectedText,
                style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(220.dp)
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(id = R.string.select_task),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(250.dp)
        ) {
            val allTasksWithoutCurrent = allTasks.filter { it != selectedTask }
            if (allTasksWithoutCurrent.isEmpty()) {
                Text(stringResource(R.string.no_tasks_found_message), Modifier.padding(8.dp))
            }
            allTasksWithoutCurrent.forEach { task ->
                DropdownMenuItem(onClick = {
                    onTaskSelected(task)
                    expanded = false
                }) {
                    val textColor =
                        if (task.isCompletedToday) MaterialTheme.colors.primary else LocalContentColor.current
                    Text(
                        stringResource(
                            R.string.task_name_with_completed_and_total_minutes,
                            task.name,
                            task.timeCompletedTodayInMinutes,
                            task.dailyGoalInMinutes
                        ),
                        color = textColor
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    if (task.isCompletedToday) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.task_completed),
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskTimeIndicator(
    task: Task?,
    taskActiveToday: Boolean,
    running: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    val timeLeftInMillis = task?.timeLeftTodayInMilliseconds ?: 0
    val timeGoalInMillis = task?.dailyGoalInMilliseconds ?: 0
    val progress = 1 - (timeLeftInMillis.toFloat() / timeGoalInMillis.toFloat())
    val completed = timeLeftInMillis <= 0

    val timeText = formatTimeText(timeLeftInMillis)
    val timeTextColor =
        if (running) MaterialTheme.colors.primary else if (!completed) LocalContentColor.current else Color.LightGray

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.sizeIn(minWidth = 230.dp, minHeight = 230.dp)
    ) {
        CircularProgressIndicatorWithBackground(
            progress = if (taskActiveToday) progress else 0f,
            strokeWidth = strokeWidth,
            modifier = Modifier.sizeIn(minWidth = 230.dp, minHeight = 230.dp)
        )
        when {
            !taskActiveToday && task != null -> {
                Text(
                    text = stringResource(R.string.task_not_active_today),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 45.dp, end = 45.dp),
                )
            }
            taskActiveToday && completed -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.completed_today),
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 45.dp, end = 45.dp),
                    )
                    Text(
                        text = stringResource(R.string.timers_will_reset_at_midnight),
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 45.dp, end = 45.dp),
                    )
                }
            }
            else -> {
                Text(text = timeText, style = MaterialTheme.typography.h3, color = timeTextColor)
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
private fun PreviewTimerScreenNoTask() {
    Just10Minutes2Theme {
        TimerBody(
            uiState = TimerUiState(
                selectedTask = null,
                taskActiveToday = true,
                allTasks = emptyList(),
                timerRunning = false,
                showSelectNewTaskConfirmationDialog = false
            ),
            onStartTimerClicked = {},
            onStopTimerClicked = {},
            onTaskSelected = {},
            onSelectNewTaskConfirmed = {},
            onDismissSelectNewTaskConfirmationDialog = {},
            onEditTaskClicked = {}
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
private fun PreviewTimerScreenCompleted() {
    Just10Minutes2Theme {
        TimerBody(
            uiState = TimerUiState(
                selectedTask = Task(
                    "Example task",
                    WeekdaySelection(allDays = true),
                    10,
                    10 * 60 * 1000L
                ),
                taskActiveToday = true,
                allTasks = emptyList(),
                timerRunning = false,
                showSelectNewTaskConfirmationDialog = false
            ),
            onStartTimerClicked = {},
            onStopTimerClicked = {},
            onTaskSelected = {},
            onSelectNewTaskConfirmed = {},
            onDismissSelectNewTaskConfirmationDialog = {},
            onEditTaskClicked = {}
        )
    }
}