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

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoTheme
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.TasksTopAppBar
import timber.log.Timber

// Main composable for tasks screen
@Composable
fun TasksScreen(
    @StringRes userMessage: Int, // Resource for user message
    onAddTask: () -> Unit, // Function to trigger adding new task
    onTaskClick: (Task) -> Unit, // Function to handle task click
    onUserMessageDisplayed: () -> Unit, // Function to handle user message display
    openDrawer: () -> Unit, // Function to open the app's drawer
    modifier: Modifier = Modifier, // Modifier to apply to layout
    viewModel: TasksViewModel = hiltViewModel(), // ViewModel for managing UI state
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() } // State for displaying snackbars
) {
    Timber.d("TasksScreen loaded")
    // Scaffold to define structure of screen
    Scaffold(
        modifier = modifier.fillMaxSize(), // Fill entire screen
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TasksTopAppBar( // Custom top app bar for filtering tasks
                openDrawer = openDrawer,
                onFilterAllTasks = {
                    Timber.d("Filter changed: Showing all tasks")
                    viewModel.setFiltering(ALL_TASKS) },
                onFilterActiveTasks = {
                    Timber.d("Filter changed: Showing active tasks")
                    viewModel.setFiltering(ACTIVE_TASKS) },
                onFilterCompletedTasks = {
                    Timber.d("Filter changed: Showing completed tasks")
                    viewModel.setFiltering(COMPLETED_TASKS) },
                onClearCompletedTasks = {
                    Timber.d("Clearing completed tasks")
                    viewModel.clearCompletedTasks() },
                onRefresh = {
                    Timber.d("Task list refreshed")
                    viewModel.refresh() }
            )
        },
        floatingActionButton = {
            // Floating action button to add new task
            SmallFloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_task))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // Content for displaying the tasks
        TasksContent(
            loading = uiState.isLoading, // Whether tasks are still loading
            tasks = uiState.items, // List of tasks to display
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel, //Label for current filter
            noTasksLabel = uiState.filteringUiInfo.noTasksLabel, // Label to display when no tasks exist
            noTasksIconRes = uiState.filteringUiInfo.noTaskIconRes, // Icon to display when no tasks exist
            onRefresh = viewModel::refresh, // Refresh callback
            onTaskClick = onTaskClick, // Task click callback
            onTaskCheckedChange = viewModel::completeTask, // Task completion change callback
            modifier = Modifier.padding(paddingValues) // Modifier to apply padding
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { message ->
            val snackbarText = stringResource(message)
            LaunchedEffect(snackbarHostState, viewModel, message, snackbarText) {
                Timber.d("Snackbar displayed: $snackbarText")
                snackbarHostState.showSnackbar(snackbarText) // Show snackbar
                viewModel.snackbarMessageShown() // Mark snackabr as shown
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage) // Display user message
                currentOnUserMessageDisplayed() // Trigger callback for when message is displayed
            }
        }
    }
}

// Content for task list
@Composable
private fun TasksContent(
    loading: Boolean,
    tasks: List<Task>, // List of tasks to be displayed
    @StringRes currentFilteringLabel: Int, // Current filter label for task list
    @StringRes noTasksLabel: Int, // Label for empty task list
    @DrawableRes noTasksIconRes: Int, // Icon for empty task list
    onRefresh: () -> Unit, // Refresh callback
    onTaskClick: (Task) -> Unit, // Task click callback
    onTaskCheckedChange: (Task, Boolean) -> Unit, // Task checked change callback
    modifier: Modifier = Modifier // Modifier for layout
) {
    LoadingContent(
        loading = loading, // Whether content is loading
        empty = tasks.isEmpty() && !loading, // Whether task list is empty
        emptyContent = { TasksEmptyContent(noTasksLabel, noTasksIconRes, modifier) }, // Display empty content if no tasks
        onRefresh = onRefresh
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
        ) {
            // Show current filter label at the top of the list
            Text(
                text = stringResource(currentFilteringLabel),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_padding),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                ),
                style = MaterialTheme.typography.headlineSmall
            )
            // LazyColumn to display tasks
            LazyColumn {
                items(tasks) { task ->
                    Timber.d("Task clicked - ID: ${task.id}, Title: ${task.title}")
                    TaskItem(
                        task = task, // Task to display
                        onTaskClick = onTaskClick, // Task click callback
                        onCheckedChange = { onTaskCheckedChange(task, it) } // Checkbox change callback
                    )
                }
            }
        }
    }
}

// Displays a single task
@Composable
private fun TaskItem(
    task: Task, // Task to display
    onCheckedChange: (Boolean) -> Unit, // Callback for checkbox change
    onTaskClick: (Task) -> Unit // Callback for task click
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, // Align checkbox and text vertically
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onTaskClick(task) } // Make task clickable to navigate to details
    ) {
        // Checkbox to mark task as completed
        Checkbox(
            checked = task.isCompleted, // Check if task is completed
            onCheckedChange = onCheckedChange // Handle checkbox change
        )
        // Display task title and strike through if completed
        Text(
            text = task.titleForList,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            ),
            textDecoration = if (task.isCompleted) {
                TextDecoration.LineThrough // Strike through applied here if completed
            } else {
                null
            }
        )
    }
}

// Content for empty task list
@Composable
private fun TasksEmptyContent(
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = noTasksIconRes),
            contentDescription = stringResource(R.string.no_tasks_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = noTasksLabel))
    }
}

//Previews for the tasks content
@Preview
@Composable
private fun TasksContentPreview() {
    MaterialTheme {
        Surface {
            TasksContent(
                loading = false,
                tasks = listOf(
                    Task(
                        title = "Title 1",
                        description = "Description 1",
                        isCompleted = false,
                        id = "ID 1"
                    ),
                    Task(
                        title = "Title 2",
                        description = "Description 2",
                        isCompleted = true,
                        id = "ID 2"
                    ),
                    Task(
                        title = "Title 3",
                        description = "Description 3",
                        isCompleted = true,
                        id = "ID 3"
                    ),
                    Task(
                        title = "Title 4",
                        description = "Description 4",
                        isCompleted = false,
                        id = "ID 4"
                    ),
                    Task(
                        title = "Title 5",
                        description = "Description 5",
                        isCompleted = true,
                        id = "ID 5"
                    ),
                ),
                currentFilteringLabel = R.string.label_all,
                noTasksLabel = R.string.no_tasks_all,
                noTasksIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onTaskClick = { },
                onTaskCheckedChange = { _, _ -> },
            )
        }
    }
}


// Preview for empty task list
@Preview
@Composable
private fun TasksContentEmptyPreview() {
    MaterialTheme {
        Surface {
            TasksContent(
                loading = false,
                tasks = emptyList(),
                currentFilteringLabel = R.string.label_all,
                noTasksLabel = R.string.no_tasks_all,
                noTasksIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onTaskClick = { },
                onTaskCheckedChange = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun TasksEmptyContentPreview() {
    TodoTheme {
        Surface {
            TasksEmptyContent(
                noTasksLabel = R.string.no_tasks_all,
                noTasksIconRes = R.drawable.logo_no_fill
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemPreview() {
    MaterialTheme {
        Surface {
            TaskItem(
                task = Task(
                    title = "Title",
                    description = "Description",
                    id = "ID"
                ),
                onTaskClick = { },
                onCheckedChange = { }
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemCompletedPreview() {
    MaterialTheme {
        Surface {
            TaskItem(
                task = Task(
                    title = "Title",
                    description = "Description",
                    isCompleted = true,
                    id = "ID"
                ),
                onTaskClick = { },
                onCheckedChange = { }
            )
        }
    }
}
