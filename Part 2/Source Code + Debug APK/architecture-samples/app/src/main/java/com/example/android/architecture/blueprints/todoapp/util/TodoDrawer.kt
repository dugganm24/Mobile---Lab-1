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

package com.example.android.architecture.blueprints.todoapp.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinations
import com.example.android.architecture.blueprints.todoapp.TodoNavigationActions
import com.example.android.architecture.blueprints.todoapp.TodoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

// Function for modal navigation drawer
@Composable
fun AppModalDrawer(
    drawerState: DrawerState, // State of navigation drawer
    currentRoute: String, // Current route in navigation
    navigationActions: TodoNavigationActions, // Actions for navigating between screens
    coroutineScope: CoroutineScope = rememberCoroutineScope(), // Coroutine scope to handle drawer state changes
    content: @Composable () -> Unit // Content to display
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Define contents of the drawer
            AppDrawer(
                currentRoute = currentRoute,
                navigateToTasks = {
                    Timber.tag("AppDrawer").d("Navigating to Tasks")
                    navigationActions.navigateToTasks() },
                navigateToStatistics = {
                    Timber.tag("AppDrawer").d("Navigating to Statistics")
                    navigationActions.navigateToStatistics() },
                closeDrawer = {
                    Timber.tag("AppDrawer").d("Closing drawer")
                    coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        content()
    }
}

// Function for drawer UI
@Composable
private fun AppDrawer(
    currentRoute: String,
    navigateToTasks: () -> Unit,
    navigateToStatistics: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = modifier.fillMaxSize()) {
            DrawerHeader() // Display the drawer header
            // Button for navigating tasks screen
            DrawerButton(
                painter = painterResource(id = R.drawable.ic_list),
                label = stringResource(id = R.string.list_title),
                isSelected = currentRoute == TodoDestinations.TASKS_ROUTE,
                action = {
                    navigateToTasks()
                    closeDrawer()
                }
            )
            // Button for navigating to statistics screen
            DrawerButton(
                painter = painterResource(id = R.drawable.ic_statistics),
                label = stringResource(id = R.string.statistics_title),
                isSelected = currentRoute == TodoDestinations.STATISTICS_ROUTE,
                action = {
                    navigateToStatistics()
                    closeDrawer()
                }
            )
        }
    }
}

// Function for drawer header
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(primaryDarkColor)
            .height(dimensionResource(id = R.dimen.header_height))
            .padding(dimensionResource(id = R.dimen.header_padding))
    ) {
        // App logo
        Image(
            painter = painterResource(id = R.drawable.logo_no_fill),
            contentDescription =
            stringResource(id = R.string.tasks_header_image_content_description),
            modifier = Modifier.width(dimensionResource(id = R.dimen.header_image_width))
        )
        // Header text
        Text(
            text = stringResource(id = R.string.navigation_view_header_title),
            color = MaterialTheme.colorScheme.surface
        )
    }
}

// Function for single button in drawer
@Composable
private fun DrawerButton(
    painter: Painter, // Button icon
    label: String, // Button label
    isSelected: Boolean, // Whether button is selected
    action: () -> Unit, // Action to perform when button is clicked
    modifier: Modifier = Modifier
) {
    val tintColor = if (isSelected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    TextButton(
        onClick = {
            Timber.tag("DrawerButton").d("Clicked: $label")
            action()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painter,
                contentDescription = null, // decorative
                tint = tintColor
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = tintColor
            )
        }
    }
}

// Preview function for drawer UI
@Preview("Drawer contents")
@Composable
fun PreviewAppDrawer() {
    TodoTheme {
        Surface {
            AppDrawer(
                currentRoute = TodoDestinations.TASKS_ROUTE,
                navigateToTasks = {},
                navigateToStatistics = {},
                closeDrawer = {}
            )
        }
    }
}
