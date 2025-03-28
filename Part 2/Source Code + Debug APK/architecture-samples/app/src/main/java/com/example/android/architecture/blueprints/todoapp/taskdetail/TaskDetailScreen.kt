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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.TaskDetailTopAppBar
import timber.log.Timber

// Main composable for task detail screen
@Composable
fun TaskDetailScreen(
    onEditTask: (String) -> Unit, // Callback to edit task
    onBack: () -> Unit, // Callback to handle back navigation
    onDeleteTask: () -> Unit, // Callback to delete task
    modifier: Modifier = Modifier, // Modifier to apply to layout to composable
    viewModel: TaskDetailViewModel = hiltViewModel(), // ViewModel instance for managing the task detail
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() } // State for displaying snackbars

) {
    Timber.d("TaskDetailScreen entered")

    // Set up the Scaffold with the top app bar, floating action button, and snackbar
    Scaffold(
        modifier = modifier.fillMaxSize(), // Fill entire screen
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TaskDetailTopAppBar(onBack = onBack, onDelete = viewModel::deleteTask) }, // Set top app bar with back and delete actions
        // Set floating action button to edit task
        floatingActionButton = {
            SmallFloatingActionButton(onClick = {
                Timber.d("Edit task clicked: ${viewModel.taskId}")
                onEditTask(viewModel.taskId) }) {
                Icon(Icons.Filled.Edit, stringResource(id = R.string.edit_task))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Collect UI state from ViewModel

        Timber.d("TaskDetailScreen UI State: $uiState")

        // Render the task details content
        EditTaskContent(
            loading = uiState.isLoading, // Pass loading state to content composable
            empty = uiState.task == null && !uiState.isLoading, // Pass empty state to content composable
            task = uiState.task, // Pass task data to content composable
            onRefresh = viewModel::refresh, // Pass refresh action to content composable
            onTaskCheck = viewModel::setCompleted, // Pass task check action to content composable
            modifier = Modifier.padding(paddingValues) // Apply padding to content
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                Timber.d("Snackbar message: $snackbarText")
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if the task is deleted and call onDeleteTask
        LaunchedEffect(uiState.isTaskDeleted) {
            if (uiState.isTaskDeleted) {
                Timber.d("Task deleted successfully")
                onDeleteTask()
            }
        }
    }
}

// Composable for content for task detail screen
@Composable
private fun EditTaskContent(
    loading: Boolean, // Whether content is loading
    empty: Boolean, // Whether content is empty
    task: Task?, // Task data to display
    onTaskCheck: (Boolean) -> Unit, // Callback to update task completion status
    onRefresh: () -> Unit, // Callback to refresh task data
    modifier: Modifier = Modifier // Modifier to apply to layout to composable
) {
    Timber.d("Rendering EditTaskContent: loading=$loading, empty=$empty, task=$task")

    val screenPadding = Modifier.padding( // Define padding for screen
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.vertical_margin),
    )
    val commonModifier = modifier // Common modifier with fillMaxWidth and screenPadding
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading, // Pass loading state to LoadingContent
        empty = empty, // Pass empty state to LoadingContent
        // Content to display when task is empty
        emptyContent = {
            Timber.d("No task data to display")
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) { // Content to display when task data is available
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

            ) {
                if (task != null) {
                    Checkbox(task.isCompleted, onTaskCheck)
                    Column {
                        Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
                        Text(text = task.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// Preview for EditTaskContent
@Preview
@Composable
private fun EditTaskContentPreview() {
    Surface {
        EditTaskContent(
            loading = false,
            empty = false,
            Task(
                title = "Title",
                description = "Description",
                isCompleted = false,
                id = "ID"
            ),
            onTaskCheck = { },
            onRefresh = { }
        )
    }

}

@Preview
@Composable
private fun EditTaskContentTaskCompletedPreview() {
    Surface {
        EditTaskContent(
            loading = false,
            empty = false,
            Task(
                title = "Title",
                description = "Description",
                isCompleted = false,
                id = "ID"
            ),
            onTaskCheck = { },
            onRefresh = { }
        )
    }
}

// Preview for EditTaskContent when task is empty
@Preview
@Composable
private fun EditTaskContentEmptyPreview() {
    Surface {
        EditTaskContent(
            loading = false,
            empty = true,
            Task(
                title = "Title",
                description = "Description",
                isCompleted = false,
                id = "ID"
            ),
            onTaskCheck = { },
            onRefresh = { }
        )
    }
}
