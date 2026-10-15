package com.josense.locktask.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.josense.locktask.R
import com.josense.locktask.ui.page.create.CreateTaskSheet
import com.josense.locktask.ui.page.home.HomeScreen
import com.josense.locktask.ui.theme.LockTaskTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            LockTaskTheme {
                var showAddTaskSheet by remember { mutableStateOf(false) }
                val addTaskSheetState = rememberModalBottomSheetState()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = {
                            Column {
                                Text(
                                    text = resources.getString(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = resources.getString(R.string.app_description),
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
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                    if (showAddTaskSheet) {
                        CreateTaskSheet(
                            onDismiss = { showAddTaskSheet = false },
                            sheetState = addTaskSheetState
                        )
                    }
                }
            }
        }
    }
}