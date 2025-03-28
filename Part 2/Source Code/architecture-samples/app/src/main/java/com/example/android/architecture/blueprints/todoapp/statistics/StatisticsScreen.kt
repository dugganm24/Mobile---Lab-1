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

package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.StatisticsTopAppBar
import timber.log.Timber

// Main composable for statistics screen
@Composable
fun StatisticsScreen(
    openDrawer: () -> Unit, // Function to open the app's drawer
    modifier: Modifier = Modifier, // Modifier for styling and layout
    viewModel: StatisticsViewModel = hiltViewModel(), // ViewModel for managing UI state
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() } // Manages snackbar state
) {
    Timber.d("StatisticsScreen loaded")

    // Scaffold to define structure of screen
    Scaffold(
        modifier = modifier.fillMaxSize(), // Fills entire screen
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Defines snackbar behavior
        topBar = {
            Timber.d("Top AppBar rendered for Statistics Screen")
            StatisticsTopAppBar(openDrawer) }, // Displays top app bar with drawer toggle
    ) { paddingValues ->
        // UI state from viewModel and observes lifecycle changes
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Timber.d("UI State updated: Loading = ${uiState.isLoading}, Empty = ${uiState.isEmpty}")

        // Passes UI state to StatisticsContent to update displayed content
        StatisticsContent(
            loading = uiState.isLoading,
            empty = uiState.isEmpty,
            activeTasksPercent = uiState.activeTasksPercent,
            completedTasksPercent = uiState.completedTasksPercent,
            onRefresh = {
                Timber.d("Refresh triggered in StatisticsScreen")
                viewModel.refresh() }, // Triggers refresh in ViewModel
            modifier = modifier.padding(paddingValues)
        )
    }
}

// Composable function to display statistics content
@Composable
private fun StatisticsContent(
    loading: Boolean, // Flag to show loading state
    empty: Boolean, // Flag to show empty state
    activeTasksPercent: Float, // Percentage of active tasks
    completedTasksPercent: Float, // Percentage of completed tasks
    onRefresh: () -> Unit, // Function to trigger refresh
    modifier: Modifier = Modifier // Modifier for styling and layout
) {
    // Create a common modifier for padding and layout
    val commonModifier = modifier
        .fillMaxSize() // Fills entire screen
        .padding(all = dimensionResource(id = R.dimen.horizontal_margin))

    Timber.d("StatisticsContent rendering - Loading: $loading, Empty: $empty")

    // Use LoadingContent composable to manage loading and empty state
    LoadingContent(
        loading = loading, // Show loading if true
        empty = empty, // Show empty if true
        onRefresh = onRefresh, // Handle refresh
        modifier = modifier, // Apply custom modifier
        emptyContent = {
            Timber.d("Empty content shown in StatisticsContent")
            Text(
                text = stringResource(id = R.string.statistics_no_tasks),
                modifier = commonModifier
            )
        }
    ) {
        // Column to display statistics
        Column(
            commonModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!loading) { // Display statistics only if not loading
                Timber.d("Displaying statistics: Active = $activeTasksPercent%, Completed = $completedTasksPercent%")
                Text(stringResource(id = R.string.statistics_active_tasks, activeTasksPercent))
                Text(
                    stringResource(
                        id = R.string.statistics_completed_tasks,
                        completedTasksPercent // Display completed tasks
                    )
                )
            }
        }
    }
}

// Preview composable to show StatisticsContent with sample data
@Preview
@Composable
fun StatisticsContentPreview() {
    Surface {
        StatisticsContent(
            loading = false,
            empty = false,
            activeTasksPercent = 80f,
            completedTasksPercent = 20f,
            onRefresh = { }
        )
    }
}

// Preview composable to show StatisticsContent with empty state
@Preview
@Composable
fun StatisticsContentEmptyPreview() {
    Surface {
        StatisticsScreen({})
    }
}
