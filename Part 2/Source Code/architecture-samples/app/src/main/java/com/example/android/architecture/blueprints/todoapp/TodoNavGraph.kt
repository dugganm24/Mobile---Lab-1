/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp

import android.app.Activity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TASK_ID_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskScreen
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsScreen
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailScreen
import com.example.android.architecture.blueprints.todoapp.tasks.TasksScreen
import com.example.android.architecture.blueprints.todoapp.util.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun TodoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(), // Controller to manage navigation
    coroutineScope: CoroutineScope = rememberCoroutineScope(), // Scope for coroutines
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed), // Navigation drawer state
    startDestination: String = TodoDestinations.TASKS_ROUTE, // First screen to display
    navActions: TodoNavigationActions = remember(navController) {
        TodoNavigationActions(navController) // Creates navigation actions
    }
) {

    Timber.tag("TodoNavGraph").d("Navigation Graph initialized")

    // Get current navigation state and extract current route
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    // Defines navigation graph structure
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Defines route for tasks screen
        composable(
            TodoDestinations.TASKS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            Timber.tag("Navigation").d("Navigated to TasksScreen")
            // Wraps screen in modal drawer
            AppModalDrawer(drawerState, currentRoute, navActions) {
                TasksScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddTask = {
                        Timber.tag("Navigation").d("Navigating to AddEditTaskScreen (Add Task)")
                        navActions.navigateToAddEditTask(R.string.add_task, null) },
                    onTaskClick = { task ->
                        Timber.tag("Navigation").d("Navigating to TaskDetailScreen for Task ID: ${task.id}")
                        navActions.navigateToTaskDetail(task.id) },
                    openDrawer = {
                        Timber.tag("Drawer").d("Opening navigation drawer")
                        coroutineScope.launch { drawerState.open() } }
                )
            }
        }

        // Defines route for statistics screen
        composable(TodoDestinations.STATISTICS_ROUTE) {
            Timber.tag("Navigation").d("Navigated to StatisticsScreen")
            AppModalDrawer(drawerState, currentRoute, navActions) {
                StatisticsScreen(openDrawer = {
                    Timber.tag("Drawer").d("Opening navigation drawer")
                    coroutineScope.launch { drawerState.open() } })
            }
        }

        // Defines route for add/edit task screen
        composable(
            TodoDestinations.ADD_EDIT_TASK_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType },
                navArgument(TASK_ID_ARG) { type = NavType.StringType; nullable = true },
            )
        ) { entry ->
            val taskId = entry.arguments?.getString(TASK_ID_ARG)
            Timber.tag("Navigation").d("Navigated to AddEditTaskScreen - Task ID: $taskId")
            AddEditTaskScreen(
                topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                onTaskUpdate = {
                    // Navigates back to task list
                    navActions.navigateToTasks(
                        if (taskId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                    )
                },
                onBack = {
                    Timber.tag("Navigation").d("Navigating back from AddEditTaskScreen")
                    navController.popBackStack() } // Navigates back to previous screen
            )
        }

        // Defines route for task detail screen
        composable(TodoDestinations.TASK_DETAIL_ROUTE) {
            Timber.tag("Navigation").d("Navigated to TaskDetailScreen")
            TaskDetailScreen(
                onEditTask = { taskId ->
                    Timber.tag("Navigation").d("Editing Task ID: $taskId")
                    navActions.navigateToAddEditTask(R.string.edit_task, taskId)
                },
                onBack = {
                    Timber.tag("Navigation").d("Navigating back from TaskDetailScreen")
                    navController.popBackStack() },
                onDeleteTask = {
                    Timber.tag("Navigation").d("Task deleted, navigating back to TasksScreen")
                    navActions.navigateToTasks(DELETE_RESULT_OK) }
            )
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
