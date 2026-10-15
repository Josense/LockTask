package com.josense.locktask.ui.page.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.page.create.CreateTaskSheet
import com.josense.locktask.ui.theme.LockTaskTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageRoot(
    onTaskClick: (Task) -> Unit,
) {

    var showAddTaskSheet by remember { mutableStateOf(false) }
    val addTaskSheetState = rememberModalBottomSheetState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.app_description),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            })
        },
        floatingActionButton =  {
            FloatingActionButton(onClick = { showAddTaskSheet = !showAddTaskSheet }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        HomeScreen(Modifier.padding(innerPadding), onTaskClick)
        if (showAddTaskSheet) {
            CreateTaskSheet(
                onDismiss = { showAddTaskSheet = false },
                sheetState = addTaskSheetState
            )
        }
    }
}