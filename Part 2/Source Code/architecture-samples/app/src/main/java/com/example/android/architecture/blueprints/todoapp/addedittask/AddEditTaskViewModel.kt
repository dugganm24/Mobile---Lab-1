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

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UiState for the Add/Edit screen
 */
data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val isTaskCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository, // Repository for task data
    savedStateHandle: SavedStateHandle // Saved state handle for retrieving passed arguments
) : ViewModel() {

    private val taskId: String? = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable Task is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddEditTaskUiState()) // Mutable state flow for UI state
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow() // Exposes UI state as a read-only StateFlow

    init {
        if (taskId != null) {
            loadTask(taskId) // Load tasks if taskId is provided
        }
    }

    // Called when clicking on fab.
    fun saveTask() {
        if (uiState.value.title.isEmpty() || uiState.value.description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_task_message) // Show error message if title or description is empty
            }
            return
        }

        if (taskId == null) {
            createNewTask() // Create new task if taskId is null
        } else {
            updateTask() // Update existing task if taskId is provided
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null) // Clear messsage after its been thrown
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(title = newTitle) // Update title in UI state
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update {
            it.copy(description = newDescription) // Update description in UI state
        }
    }

    private fun createNewTask() = viewModelScope.launch {
        taskRepository.createTask(uiState.value.title, uiState.value.description) // Create new task in repositoru
        _uiState.update {
            it.copy(isTaskSaved = true) // Set to true after saving
        }
    }

    private fun updateTask() {
        if (taskId == null) {
            throw RuntimeException("updateTask() was called but task is new.") // Throw exception if taskId is null
        }
        viewModelScope.launch {
            taskRepository.updateTask(
                taskId,
                title = uiState.value.title,
                description = uiState.value.description,
            )
            _uiState.update {
                it.copy(isTaskSaved = true) // Set to true after updating
            }
        }
    }

    private fun loadTask(taskId: String) {
        _uiState.update {
            it.copy(isLoading = true) // Set isLoading to true while loading
        }
        viewModelScope.launch {
            taskRepository.getTask(taskId).let { task ->
                if (task != null) {
                    _uiState.update {
                        it.copy(
                            title = task.title,
                            description = task.description,
                            isTaskCompleted = task.isCompleted,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false) // Set faslse if task is not found
                    }
                }
            }
        }
    }
}
