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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UiState for the Details screen.
 */
data class TaskDetailUiState(
    val task: Task? = null, // Task to display
    val isLoading: Boolean = false, // Loading state
    val userMessage: Int? = null, // User message to display
    val isTaskDeleted: Boolean = false // Indicates if task has been deleted
)

/**
 * ViewModel for the Details screen.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository, // Repository for task data operations
    savedStateHandle: SavedStateHandle // Saved state handle for retrieving navigation arguments
) : ViewModel() {

    val taskId: String = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!! // Retrieve task ID from the saved state

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null) // Mutable state flow for user messages
    private val _isLoading = MutableStateFlow(false) // Mutable state flow for loading state
    private val _isTaskDeleted = MutableStateFlow(false) // Mutable state flow for task deletion state
    private val _taskAsync = taskRepository.getTaskStream(taskId) // Flow for representing the task from the repository
        .map { handleTask(it) } // Map the task to an Async state
        .catch { emit(Async.Error(R.string.loading_task_error)) } // Catch any errors and emit an error state

    val uiState: StateFlow<TaskDetailUiState> = combine(
        _userMessage, _isLoading, _isTaskDeleted, _taskAsync
    ) { userMessage, isLoading, isTaskDeleted, taskAsync ->
        when (taskAsync) {
            Async.Loading -> {
                TaskDetailUiState(isLoading = true) // Loading state
            }
            is Async.Error -> {
                TaskDetailUiState(
                    userMessage = taskAsync.errorMessage, // Error state with error message
                    isTaskDeleted = isTaskDeleted
                )
            }
            is Async.Success -> {
                TaskDetailUiState(
                    task = taskAsync.data, // Success state with task data
                    isLoading = isLoading,
                    userMessage = userMessage,
                    isTaskDeleted = isTaskDeleted
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope, // Use ViewModel scope
            started = WhileUiSubscribed, // Collect the flow while UI is subscribed
            initialValue = TaskDetailUiState(isLoading = true) // Initial loading state
        )

    // Delete task from repository
    fun deleteTask() = viewModelScope.launch {
        taskRepository.deleteTask(taskId)
        _isTaskDeleted.value = true
    }

    // Set task as completed or active
    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = uiState.value.task ?: return@launch
        if (completed) {
            taskRepository.completeTask(task.id)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(task.id)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    // Refresh task data from repository
    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            taskRepository.refreshTask(taskId)
            _isLoading.value = false
        }
    }

    // Clear user message
    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    // Set user message
    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }


    private fun handleTask(task: Task?): Async<Task?> {
        if (task == null) {
            return Async.Error(R.string.task_not_found) // Return error if task null
        }
        return Async.Success(task) // Return task if success state
    }
}
