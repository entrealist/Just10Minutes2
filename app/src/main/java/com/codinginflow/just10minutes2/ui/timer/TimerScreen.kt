package com.codinginflow.just10minutes2.ui.timer

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.ui.theme.Just10Minutes2Theme

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {

    TimerBody()
}

@Composable
private fun TimerBody(
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
        BodyContent()
    }
}

@Composable
private fun BodyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DropdownSelector()
        CircularTextTimer(progress = 0.8f, text = "02:00")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /*TODO*/ }) {
            Text(stringResource(R.string.start_timer))
        }
    }
}

@Composable
private fun DropdownSelector(

) {
    val width: Dp = 250.dp

    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text("Selected Task")
            Spacer(Modifier.width(2.dp))
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(id = R.string.select_task),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(width)
        ) {
            DropdownMenuItem(onClick = { expanded = false }) {
                Text(text = "Other task")
            }
            DropdownMenuItem(onClick = { expanded = false }) {
                Text(text = "Yet another task")
            }
        }
    }
}

@Composable
private fun CircularTextTimer(
    progress: Float,
    text: String,
    size: Dp = 200.dp,
    strokeWidth: Dp = 10.dp
) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator( // background
            progress = 1f,
            strokeWidth = strokeWidth,
            modifier = Modifier.size(size),
            color = Color.LightGray
        )
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = strokeWidth,
            modifier = Modifier.size(size)
        )
        Text(text, style = MaterialTheme.typography.h3)
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
        TimerBody()
    }
}