package com.josense.locktask.ui.page.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.rive.Fit
import app.rive.Result
import app.rive.Rive
import app.rive.RiveFileSource
import app.rive.ViewModelSource
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorkerOrNull
import app.rive.rememberViewModelInstanceResult
import com.josense.locktask.R

@Composable
internal fun EmptyTaskIllustration(modifier: Modifier = Modifier) {
    // Reserve the artwork's aspect ratio while loading, including in IDE previews.
    Box(modifier = modifier.aspectRatio(960f / 400f).clearAndSetSemantics { }) {
        if (LocalInspectionMode.current) return@Box

        val workerError = remember { mutableStateOf<Throwable?>(null) }
        val worker = rememberRiveWorkerOrNull(workerError) ?: return@Box
        when (val file = rememberRiveFile(
            RiveFileSource.RawRes.from(R.raw.task_empty_companion),
            worker,
        )) {
            is Result.Success -> {
                val viewModel = rememberViewModelInstanceResult(
                    file.value,
                    ViewModelSource.Named("CompanionState").defaultInstance(),
                )
                if (viewModel is Result.Success) {
                    Rive(
                        file.value,
                        viewModelInstance = viewModel.value,
                        fit = Fit.Contain(),
                        modifier = Modifier.fillMaxSize().testTag("empty_task_animation"),
                    )
                }
            }
            is Result.Loading, is Result.Error -> Unit // The empty-state text remains available.
        }
    }
}
