package com.codinginflow.just10minutes2.addedittask

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
    navigateBackWithResult: (AddEditTaskViewModel.AddEditTaskResult) -> Unit,
) {
    val taskNameInput by viewModel.taskNameInput.observeAsState()

    val title = stringResource(if (viewModel.taskId == Task.NO_ID) R.string.add_task else R.string.edit_task)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddEditTaskViewModel.Event.NavigateBackWithResult -> TODO()
                is AddEditTaskViewModel.Event.ShowInvalidInputMessage -> TODO()
                is AddEditTaskViewModel.Event.NavigateUp -> navigateUp()
            }
        }
    }

    AddEditTaskBody(
        title = title,
        taskNameInput = taskNameInput,
        onTaskNameInputChanged = viewModel::setTaskName,
        onNavigateUpClick = viewModel::onNavigateUpClicked
    )
}

@Composable
private fun AddEditTaskBody(
    title: String,
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateUpClick) {
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
            taskNameInput = taskNameInput,
            onTaskNameInputChanged = onTaskNameInputChanged,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BodyContent(
    taskNameInput: String?,
    onTaskNameInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(8.dp)) {
        OutlinedTextField(
            value = taskNameInput.orEmpty(),
            onValueChange = onTaskNameInputChanged,
            label = { Text(stringResource(R.string.task_name)) },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
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
private fun PreviewTaskListScreen() {
    Just10Minutes2Theme {
        AddEditTaskBody(
            title = "Preview",
            taskNameInput = null,
            onTaskNameInputChanged = {},
            onNavigateUpClick = {}
        )
    }
}