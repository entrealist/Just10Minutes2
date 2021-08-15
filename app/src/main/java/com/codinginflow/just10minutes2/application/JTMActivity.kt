package com.codinginflow.just10minutes2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
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
import com.codinginflow.just10minutes2.addedittask.ui.AddEditTaskViewModel
import com.codinginflow.just10minutes2.application.DayCheckerSharedViewModel
import com.codinginflow.just10minutes2.application.TimerSharedViewModel
import com.codinginflow.just10minutes2.archive.ArchiveScreen
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.timer.ui.TimerScreen
import com.codinginflow.just10minutes2.tasklist.ui.TaskListScreen
import com.codinginflow.just10minutes2.common.ui.theme.Just10Minutes2Theme
import com.codinginflow.just10minutes2.statistics.ui.StatisticsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Just10Minutes2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val timerSharedViewModel: TimerSharedViewModel = hiltViewModel()
                    val dayCheckerSharedViewModel: DayCheckerSharedViewModel = hiltViewModel()
                    JTMActivityBody(timerSharedViewModel)
                }
            }
        }
    }
}

@Composable
private fun JTMActivityBody(
    timerSharedViewModel: TimerSharedViewModel
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
        JTMNavHost(navController, timerSharedViewModel, Modifier.padding(innerPadding))
    }
}

@Composable
private fun JTMNavHost(
    navController: NavHostController,
    timerSharedViewModel: TimerSharedViewModel,
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
            TimerScreen(timerSharedViewModel = timerSharedViewModel)
        }
        composable(
            route = BottomNavDestination.TaskList.route,
            arguments = listOf(
                navArgument(ARG_SHOW_BOTTOM_NAV) {
                    defaultValue = true
                }
            )) { navBackStackEntry ->
            TaskListScreen(
                timerSharedViewModel = timerSharedViewModel,
                addNewTask = {
                    navController.navigate(AppDestination.AddEditTask.route)
                },
                editTask = { taskId ->
                    navController.navigate(AppDestination.AddEditTask.route + "?$ARG_TASK_ID=$taskId")
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
                },
                navigateToArchive = {
                    navController.navigate(
                        route = AppDestination.Archive.route
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
            StatisticsScreen()
        }
        composable(
            route = AppDestination.AddEditTask.route + "?$ARG_TASK_ID={$ARG_TASK_ID}",
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
        composable(
            route = AppDestination.Archive.route,
        ) {
            ArchiveScreen(
                navigateUp = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private val bottomNavDestinations = listOf(
    BottomNavDestination.Timer,
    BottomNavDestination.TaskList,
    BottomNavDestination.Statistics
)

sealed class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    object TaskList : BottomNavDestination("TaskList", R.string.tasks, Icons.Default.List)
    object Timer : BottomNavDestination("Timer", R.string.timer, Icons.Default.Timer)
    object Statistics :
        BottomNavDestination("Statistics", R.string.statistics, Icons.Default.Assessment)
}

sealed class AppDestination(
    val route: String
) {
    object AddEditTask : AppDestination("AddEditTask")
    object Archive : AppDestination("Archive")
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