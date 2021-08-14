package com.codinginflow.just10minutes2.timer.ui

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.CircularProgressIndicatorWithBackground
import com.codinginflow.just10minutes2.common.ui.SharedViewModel
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import com.codinginflow.just10minutes2.common.util.formatTimeText
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
) {
    val activeTask by viewModel.activeTask.collectAsState(null)
    val allTasks by viewModel.allTasks.collectAsState(emptyList())
    val timerRunning by viewModel.timerRunning.collectAsState(false)

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val showSelectNewTaskConfirmationDialog by viewModel.showSelectNewTaskConfirmationDialog.observeAsState(
        false
    )

    LaunchedEffect(Unit) {
        sharedViewModel.taskToOpenInTimer.collectLatest { task ->
            viewModel.onTaskSelected(task)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimerViewModel.Event.ShowTimerStoppedMessage ->
                    scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.timer_stopped))
            }
        }
    }

    TimerBody(
        activeTask = activeTask,
        allTasks = allTasks,
        timerRunning = timerRunning,
        onStartTimerClicked = viewModel::onStartTimerClicked,
        onStopTimerClicked = viewModel::onStopTimerClicked,
        onTaskSelected = viewModel::onTaskSelected,
        showSelectNewTaskConfirmationDialog = showSelectNewTaskConfirmationDialog,
        onDismissSelectNewTaskConfirmationDialog = viewModel::onDismissSelectNewTaskConfirmationDialog,
        onSelectNewTaskConfirmed = viewModel::onSelectNewTaskConfirmed,
        scaffoldState = scaffoldState,
    )
}

@Composable
private fun TimerBody(
    activeTask: Task?,
    allTasks: List<Task>,
    timerRunning: Boolean,
    onStartTimerClicked: () -> Unit,
    onStopTimerClicked: () -> Unit,
    onTaskSelected: (Task) -> Unit,
    showSelectNewTaskConfirmationDialog: Boolean,
    onDismissSelectNewTaskConfirmationDialog: () -> Unit,
    onSelectNewTaskConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timer)) },
            )
        },
    ) {
        BodyContent(
            task = activeTask,
            allTasks = allTasks,
            timerRunning = timerRunning,
            onTaskSelected = onTaskSelected,
            onStartTimerClicked = onStartTimerClicked,
            onStopTimerClicked = onStopTimerClicked
        )

        if (showSelectNewTaskConfirmationDialog) {
            AlertDialog(
                onDismissRequest = onDismissSelectNewTaskConfirmationDialog,
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
            )
        }
    }
}

@Composable
private fun BodyContent(
    task: Task?,
    allTasks: List<Task>,
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
            Text(stringResource(R.string.active_task_colon))
            Spacer(Modifier.height(8.dp))
            DropdownMenuWithSelector(
                selectedTask = task,
                allTasks = allTasks,
                onTaskSelected = onTaskSelected,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.daily_minutes_goal_placeholder,
                    task?.dailyGoalInMinutes ?: 0
                )
            )
            Text(
                stringResource(
                    R.string.minutes_completed_today_placeholder,
                    task?.timeCompletedTodayInMinutes ?: 0
                )
            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                CircularTextTimer(
                    timeLeftInMillis = task?.timeLeftTodayInMilliseconds ?: 0,
                    timeGoalInMillis = task?.dailyGoalInMilliseconds ?: 0,
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
            val buttonEnabled = task != null && !task.isCompletedToday
            val buttonOnClick = if (timerRunning) onStopTimerClicked else onStartTimerClicked
            val buttonTextRes = if (timerRunning) R.string.stop_timer else R.string.start_timer
            Button(
                onClick = buttonOnClick,
                enabled = buttonEnabled,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(buttonTextRes))
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
            allTasks.forEach { task ->
                if (task != selectedTask) {
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
                                tint = MaterialTheme.colors.primary
                            )
                        }
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
    running: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    val active = timeGoalInMillis > 0
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
            progress = if (active) progress else 0f,
            strokeWidth = strokeWidth,
            modifier = Modifier.sizeIn(minWidth = 230.dp, minHeight = 230.dp)
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
            activeTask = null,
            allTasks = emptyList(),
            timerRunning = false,
            onStartTimerClicked = {},
            onStopTimerClicked = {},
            onTaskSelected = {},
            showSelectNewTaskConfirmationDialog = false,
            onSelectNewTaskConfirmed = {},
            onDismissSelectNewTaskConfirmationDialog = {}
        )
    }
}