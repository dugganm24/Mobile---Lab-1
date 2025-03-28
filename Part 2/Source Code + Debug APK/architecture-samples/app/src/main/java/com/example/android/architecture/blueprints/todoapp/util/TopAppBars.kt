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

package com.example.android.architecture.blueprints.todoapp.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoTheme

// Composable function for creating top app bar for tasks screen
@Composable
fun TasksTopAppBar(
    openDrawer: () -> Unit, // Callback to open navigation drawer
    onFilterAllTasks: () -> Unit, // Callback to filter all tasks
    onFilterActiveTasks: () -> Unit, // Callback to filter active tasks
    onFilterCompletedTasks: () -> Unit, // Callback to filter completed tasks
    onClearCompletedTasks: () -> Unit, // Callback to clear completed tasks
    onRefresh: () -> Unit // Callback to refresh tasks
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) }, // Set title
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer)) // Provide menu icon to open drawer
            }
        },
        actions = {
            FilterTasksMenu(onFilterAllTasks, onFilterActiveTasks, onFilterCompletedTasks) // Adds filter menu
            MoreTasksMenu(onClearCompletedTasks, onRefresh) // Adds more actions menu
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable function for the filter tasks dropdown menu
@Composable
private fun FilterTasksMenu(
    onFilterAllTasks: () -> Unit, // Callback to filter tasks
    onFilterActiveTasks: () -> Unit, // Callback to filter active tasks
    onFilterCompletedTasks: () -> Unit // Callback to filter completed tasks
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(
                painterResource(id = R.drawable.ic_filter_list), // Uses custom filter list icon
                stringResource(id = R.string.menu_filter)
            )
        }
    ) { closeMenu ->
        DropdownMenuItem(onClick = { onFilterAllTasks(); closeMenu() }, // Filter for all tasks
            text = { Text(text = stringResource(id = R.string.nav_all)) }
        )
        DropdownMenuItem(onClick = { onFilterActiveTasks(); closeMenu() }, // Filter for active tasks
            text = { Text(text = stringResource(id = R.string.nav_active)) }
        )
        DropdownMenuItem(onClick = { onFilterCompletedTasks(); closeMenu() }, // Filter for completed tasks
            text = { Text(text = stringResource(id = R.string.nav_completed)) }
        )
    }
}

// Composable function for more actions dropdown menu
@Composable
private fun MoreTasksMenu(
    onClearCompletedTasks: () -> Unit, // Callback to clear completed tasks
    onRefresh: () -> Unit // Callback to refresh tasks list
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more)) // Uses more vertical icon
        }
    ) { closeMenu ->
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.menu_clear)) }, // Clear completed tasks option
            onClick = { onClearCompletedTasks(); closeMenu() }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.refresh)) }, // Refresh option
            onClick = { onRefresh(); closeMenu() }
        )
    }
}

// Composable function for the top app bar dropdown menu
@Composable
private fun TopAppBarDropdownMenu(
    iconContent: @Composable () -> Unit, // Composable for the icon content
    content: @Composable ColumnScope.(() -> Unit) -> Unit // Composable for the dropdown content
) {
    var expanded by remember { mutableStateOf(false) } // Manages expanded state of dropdown

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) { // Wraps icon and dropdown 
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            content { expanded = !expanded }
        }
    }
}
// Composable function for the statistics top app bar
@Composable
fun StatisticsTopAppBar(openDrawer: () -> Unit) { // Callback to open navigation drawer
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.statistics_title)) }, // Sets title
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer)) // Menu icon to open drawer
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable function for top app bar for task details screen
@Composable
fun TaskDetailTopAppBar(onBack: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.task_details))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, stringResource(id = R.string.menu_delete_task))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable function for top app bar for add/edit task screen
@Composable
fun AddEditTaskTopAppBar(@StringRes title: Int, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Composable function for previewing top app bars
@Preview
@Composable
private fun TasksTopAppBarPreview() {
    TodoTheme {
        Surface {
            TasksTopAppBar({}, {}, {}, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun StatisticsTopAppBarPreview() {
    TodoTheme {
        Surface {
            StatisticsTopAppBar { }
        }
    }
}

@Preview
@Composable
private fun TaskDetailTopAppBarPreview() {
    TodoTheme {
        Surface {
            TaskDetailTopAppBar({ }, { })
        }
    }
}

@Preview
@Composable
private fun AddEditTaskTopAppBarPreview() {
    TodoTheme {
        Surface {
            AddEditTaskTopAppBar(R.string.add_task) { }
        }
    }
}
