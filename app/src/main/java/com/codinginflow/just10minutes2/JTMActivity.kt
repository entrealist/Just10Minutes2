package com.codinginflow.just10minutes2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codinginflow.just10minutes2.ui.timer.TimerScreen
import com.codinginflow.just10minutes2.ui.tasklist.TaskListScreen
import com.codinginflow.just10minutes2.ui.theme.Just10Minutes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Just10Minutes2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    JTMActivityBody()
                }
            }
        }
    }
}

@Composable
private fun JTMActivityBody() {
    val navController = rememberNavController()
    var selectedBottomNavIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                bottomNavDestinations.forEachIndexed { index, item ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = { Text(stringResource(item.titleRes)) },
                        selected = selectedBottomNavIndex == index,
                        alwaysShowLabel = false,
                        onClick = {
                            if (selectedBottomNavIndex != index) {
                                selectedBottomNavIndex = index
                                navController.navigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        JTMNavHost(navController, Modifier.padding(innerPadding))
    }
}

@Composable
private fun JTMNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = bottomNavDestinations[0].route,
        modifier = modifier
    ) {
        composable(BottomNavDestination.Timer.route) {
            TimerScreen()
        }
        composable(BottomNavDestination.TaskList.route) {
            TaskListScreen()
        }
    }
}

private val bottomNavDestinations = listOf(
    BottomNavDestination.Timer,
    BottomNavDestination.TaskList
)

sealed class BottomNavDestination(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object TaskList : BottomNavDestination("TaskList", R.string.title_task_list, Icons.Filled.List)
    object Timer : BottomNavDestination("Timer", R.string.title_timer, Icons.Filled.Timer)
}