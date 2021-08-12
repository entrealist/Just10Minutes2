package com.codinginflow.just10minutes2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navOptions
import com.codinginflow.just10minutes2.addedittask.AddEditTaskScreen
import com.codinginflow.just10minutes2.addedittask.AddEditTaskViewModel
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.ui.SharedViewModel
import com.codinginflow.just10minutes2.timer.ui.TimerScreen
import com.codinginflow.just10minutes2.tasklist.ui.TaskListScreen
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Just10Minutes2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val sharedViewModel: SharedViewModel = hiltViewModel()
                    JTMActivityBody(sharedViewModel)
                }
            }
        }
    }
}

@Composable
private fun JTMActivityBody(
    sharedViewModel: SharedViewModel
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomNav = navBackStackEntry?.arguments?.get(ARG_SHOW_BOTTOM_NAV) == true

            if (showBottomNav) {
                BottomNavigation {
                    bottomNavDestinations.forEach { destination ->
                        BottomNavigationItem( // Followed: https://developer.android.com/jetpack/compose/navigation#bottom-nav
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(
                                    route = destination.route,
                                    navOptions = createNavOptionsForBottomNavigation(navController)
                                )
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        JTMNavHost(navController, sharedViewModel, Modifier.padding(innerPadding))
    }
}

@Composable
private fun JTMNavHost(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = bottomNavDestinations[0].route,
        modifier = modifier
    ) {
        composable(
            route = BottomNavDestination.Timer.route,
            arguments = listOf(
                navArgument(ARG_SHOW_BOTTOM_NAV) {
                    defaultValue = true
                }
            )
        ) {
            TimerScreen(sharedViewModel = sharedViewModel)
        }
        composable(
            route = BottomNavDestination.TaskList.route,
            arguments = listOf(
                navArgument(ARG_SHOW_BOTTOM_NAV) {
                    defaultValue = true
                }
            )) { navBackStackEntry ->
            TaskListScreen(
                sharedViewModel = sharedViewModel,
                addNewTask = {
                    navController.navigate(AppDestinations.AddEditTask.route)
                },
                editTask = { taskId ->
                    navController.navigate(AppDestinations.AddEditTask.route + "?$ARG_TASK_ID=$taskId")
                },
                addEditResult = navBackStackEntry.savedStateHandle.get<AddEditTaskViewModel.AddEditTaskResult>(
                    KEY_ADD_EDIT_RESULT
                ),
                onAddEditResultProcessed = {
                    navBackStackEntry.savedStateHandle.remove<AddEditTaskViewModel.AddEditTaskResult>(
                        KEY_ADD_EDIT_RESULT
                    )
                },
                navigateToTimer = {
                    navController.navigate(
                        route = BottomNavDestination.Timer.route,
                        navOptions = createNavOptionsForBottomNavigation(navController)
                    )
                }
            )
        }
        composable(
            route = BottomNavDestination.Statistics.route,
            arguments = listOf(
                navArgument(ARG_SHOW_BOTTOM_NAV) {
                    defaultValue = true
                }
            )) {
            Text("Statistics")
        }
        composable(
            route = AppDestinations.AddEditTask.route + "?$ARG_TASK_ID={$ARG_TASK_ID}",
            arguments = listOf(
                navArgument(ARG_TASK_ID) {
                    type = NavType.LongType
                    defaultValue = Task.NO_ID
                }
            )
        ) {
            AddEditTaskScreen(
                navigateUp = {
                    navController.popBackStack()
                },
                navigateBackWithResult = { result ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        KEY_ADD_EDIT_RESULT,
                        result
                    )
                    navController.popBackStack()
                })
        }
    }
}

private val bottomNavDestinations = listOf(
    BottomNavDestination.Timer,
    BottomNavDestination.TaskList,
    BottomNavDestination.Statistics
)

private val fullScreenDestinations = listOf(
    AppDestinations.AddEditTask
)

sealed class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    object TaskList : BottomNavDestination("TaskList", R.string.title_task_list, Icons.Filled.List)
    object Timer : BottomNavDestination("Timer", R.string.title_timer, Icons.Filled.Timer)
    object Statistics :
        BottomNavDestination("Statistics", R.string.title_statistics, Icons.Filled.Assessment)
}

sealed class AppDestinations(
    val route: String
) {
    object AddEditTask : AppDestinations("AddEditTask")
}

private fun createNavOptionsForBottomNavigation(navController: NavHostController): NavOptions {
    return navOptions {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

const val ARG_TASK_ID = "taskId"
const val ARG_SHOW_BOTTOM_NAV = "showBottomNav"

const val KEY_ADD_EDIT_RESULT = "KEY_ADD_EDIT_RESULT"