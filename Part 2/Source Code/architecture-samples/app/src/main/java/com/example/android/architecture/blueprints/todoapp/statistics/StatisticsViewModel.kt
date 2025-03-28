/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UiState for the statistics screen.
 */
data class StatisticsUiState(
    val isEmpty: Boolean = false, // Whether the list of tasks is empty
    val isLoading: Boolean = false, // Whether data is being loaded
    val activeTasksPercent: Float = 0f, // Percentage of active tasks
    val completedTasksPercent: Float = 0f // Percentage of completed tasks
)

/**
 * ViewModel for the statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val taskRepository: TaskRepository // Repository for task data
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> =
        taskRepository.getTasksStream() // Get stream of tasks from repository
            .map { Async.Success(it) } // Map list of tasks to a success state
            .catch<Async<List<Task>>> { emit(Async.Error(R.string.loading_tasks_error)) } // Catch errors and emit error state
            .map { taskAsync -> produceStatisticsUiState(taskAsync) } // Map async state to StatisticsUiState
            .stateIn(
                scope = viewModelScope, // Use ViewModel's scope
                started = WhileUiSubscribed, // Collect the flow while the UI is subscribed
                initialValue = StatisticsUiState(isLoading = true) // Initial state
            )

    // Refresh task data from repository
    fun refresh() {
        viewModelScope.launch {
            taskRepository.refresh()
        }
    }

    private fun produceStatisticsUiState(taskLoad: Async<List<Task>>) =
        when (taskLoad) {
            Async.Loading -> {
                StatisticsUiState(isLoading = true, isEmpty = true) // Loading state
            }
            is Async.Error -> {
                // TODO: Show error message?
                StatisticsUiState(isEmpty = true, isLoading = false) // Error state
            }
            is Async.Success -> {
                val stats = getActiveAndCompletedStats(taskLoad.data) // Calculate statistics from the task list
                StatisticsUiState(
                    isEmpty = taskLoad.data.isEmpty(), // Set isEmpty based on task list
                    activeTasksPercent = stats.activeTasksPercent, // Set percentage of active tasks
                    completedTasksPercent = stats.completedTasksPercent, // Set percentage of completed tasks
                    isLoading = false // Set false after loading
                )
            }
        }
}
