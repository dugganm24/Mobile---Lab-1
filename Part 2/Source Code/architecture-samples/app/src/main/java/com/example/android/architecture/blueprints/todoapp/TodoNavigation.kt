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

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TASK_ID_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.todoapp.TodoScreens.ADD_EDIT_TASK_SCREEN
import com.example.android.architecture.blueprints.todoapp.TodoScreens.STATISTICS_SCREEN
import com.example.android.architecture.blueprints.todoapp.TodoScreens.TASKS_SCREEN
import com.example.android.architecture.blueprints.todoapp.TodoScreens.TASK_DETAIL_SCREEN
import timber.log.Timber

/**
 * Screens used in [TodoDestinations]
 */

// Screens used in the app
private object TodoScreens {
    const val TASKS_SCREEN = "tasks"
    const val STATISTICS_SCREEN = "statistics"
    const val TASK_DETAIL_SCREEN = "task"
    const val ADD_EDIT_TASK_SCREEN = "addEditTask"
}

/**
 * Arguments used in [TodoDestinations] routes
 */

// Argument names used in routes
object TodoDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val TASK_ID_ARG = "taskId"
    const val TITLE_ARG = "title"
}

/**
 * Destinations used in the [TodoActivity]
 */

// Actual routes for app's navigation
object TodoDestinations {
    const val TASKS_ROUTE = "$TASKS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val STATISTICS_ROUTE = STATISTICS_SCREEN
    const val TASK_DETAIL_ROUTE = "$TASK_DETAIL_SCREEN/{$TASK_ID_ARG}"
    const val ADD_EDIT_TASK_ROUTE = "$ADD_EDIT_TASK_SCREEN/{$TITLE_ARG}?$TASK_ID_ARG={$TASK_ID_ARG}"
}

/**
 * Models the navigation actions in the app.
 */

// Handles logic for navigating between screens in app
class TodoNavigationActions(private val navController: NavHostController) {

    // Navigates to tasks screen with optional user message
    fun navigateToTasks(userMessage: Int = 0) {
        Timber.d("navigateToTasks() called with userMessage = $userMessage")
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            TASKS_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {

            // Pop back stack to start destination (avoid stack overflow)
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    // Navigates to statistics screen
    fun navigateToStatistics() {
        Timber.d("navigateToStatistics() called")
        navController.navigate(TodoDestinations.STATISTICS_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    // Navigates to task detail screen
    fun navigateToTaskDetail(taskId: String) {
        Timber.d("navigateToTaskDetail() called with taskId = $taskId")
        navController.navigate("$TASK_DETAIL_SCREEN/$taskId")
    }

    // Navigates to add/edit task screen
    fun navigateToAddEditTask(title: Int, taskId: String?) {
        Timber.d("navigateToAddEditTask() called with title = $title, taskId = $taskId")
        navController.navigate(
            "$ADD_EDIT_TASK_SCREEN/$title".let {
                if (taskId != null) "$it?$TASK_ID_ARG=$taskId" else it
            }
        )
    }
}
