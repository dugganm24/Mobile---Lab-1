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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.AddEditTaskTopAppBar
import timber.log.Timber

// Main composable for add/edit task screen
@Composable
fun AddEditTaskScreen(
    @StringRes topBarTitle: Int, // Title for top app bar
    onTaskUpdate: () -> Unit, // Callback to trigger updating task
    onBack: () -> Unit, // Callback to handle back navigation
    modifier: Modifier = Modifier, // Modifier to apply to layout
    viewModel: AddEditTaskViewModel = hiltViewModel(), // ViewModel instance to manage screen state
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() } // State for displaying snackbars
) {
    Timber.d("AddEditTaskScreen: Composable function started")

    Scaffold(
        modifier = modifier.fillMaxSize(), // Fill entire screen
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Configure snackbar host
        topBar = { AddEditTaskTopAppBar(topBarTitle, onBack) }, // Set top app bar with title and back button
        floatingActionButton = { // Floating action button to save task
            SmallFloatingActionButton(onClick = viewModel::saveTask) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.cd_save_task)) // Display save icon
            }
        }
    ) { paddingValues -> // Padding values provided by Scaffold to avoid overlap
        val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Collect UI state from ViewModel

        Timber.d("AddEditTaskScreen: UI state changed: $uiState")


        AddEditTaskContent(
            loading = uiState.isLoading, // Pass loading state to content composable
            title = uiState.title, // Pass task title to content composable
            description = uiState.description, // Pass task description to content composable
            onTitleChanged = viewModel::updateTitle, // Pass callback to update task title
            onDescriptionChanged = viewModel::updateDescription, // Pass callback to update task description
            modifier = Modifier.padding(paddingValues) // Apply padding to content
        )

        // Check if the task is saved and call onTaskUpdate event
        LaunchedEffect(uiState.isTaskSaved) {
            if (uiState.isTaskSaved) {
                Timber.d("AddEditTaskScreen: Task saved, triggering onTaskUpdate")
                onTaskUpdate() // Trigger callback to update task
            }
        }

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage) // Get string resource for message
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                Timber.d("AddEditTaskScreen: Displaying snackbar with message: $snackbarText")
                snackbarHostState.showSnackbar(snackbarText) // Show snackbar with the message
                viewModel.snackbarMessageShown() // Notify ViewModel that the message has been displayed
            }
        }
    }
}

// Content for add/edit task screen
@Composable
private fun AddEditTaskContent(
    loading: Boolean, // Whether content is loading
    title: String, // Task title
    description: String, // Task description
    onTitleChanged: (String) -> Unit, // Callback to update task title
    onDescriptionChanged: (String) -> Unit, // Callback to update task description
    modifier: Modifier = Modifier // Modifier for composable layout
) {
    Timber.d("AddEditTaskContent: Composable function started with loading state: $loading")

    var isRefreshing by remember { mutableStateOf(false) } // State for pull-to-refresh
    val refreshingState = rememberPullToRefreshState() // State for pull-to-refresh box
    if (loading) {
        Timber.d("AddEditTaskContent: Pull-to-refresh displayed as loading is true")
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = refreshingState,
            onRefresh = { /* DO NOTHING */ },
            content = { }
        )
    } else { // If screen is not loading, display task details input fields
        Timber.d("AddEditTaskContent: Displaying task details input fields")

        Column(
            modifier // Apply provided modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            val textFieldColors = OutlinedTextFieldDefaults.colors( // Customize text field colors
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSecondary
            )
            OutlinedTextField(
                value = title, // Current title value
                modifier = Modifier.fillMaxWidth(), // Fill width of parent
                onValueChange = onTitleChanged, // Callback for title change
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title_hint),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall
                    .copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                colors = textFieldColors
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = { Text(stringResource(id = R.string.description_hint)) },
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth(),
                colors = textFieldColors
            )
        }
    }
}
