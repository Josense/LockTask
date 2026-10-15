package com.josense.locktask.ui.page.task_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.ui.page.home.EmptyHomeScreen
import com.josense.locktask.ui.theme.LockTaskTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailRoot(
    viewModel: TaskDetailViewModel = koinViewModel(),
    onBackClick: (result: TaskDetailResult?) -> Unit,
) {
    val state = viewModel.taskListState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    if (state.value == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.no_task))
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.task_detail)
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.onAction(TaskDetailAction.Complete)
                            onBackClick(TaskDetailResult(state.value!!.id, true))
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.DoneOutline,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = {

                        }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { onBackClick(null) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier.padding(it)
            ) {
                Text(text = state.value!!.title)
            }
        }
    }

}