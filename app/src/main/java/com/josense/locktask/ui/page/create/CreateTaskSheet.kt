package com.josense.locktask.ui.page.create

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.LockNotifyIconBtnWithTooltip
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import com.josense.locktask.ui.theme.extendedTypography
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
) {
    val createTaskViewModel: CreateTaskViewModel = koinViewModel()
    // Panel entry follows IME progress instead of running a second animation.
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        var textInput by remember { mutableStateOf("") }
        var needLockNotify by remember { mutableStateOf(true) }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val windowInfo = LocalWindowInfo.current
        var inputPlaced by remember { mutableStateOf(false) }
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val hardwareKeyboardAvailable = configuration.keyboard != Configuration.KEYBOARD_NOKEYS &&
            configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        val imeInsets = WindowInsets.ime
        val imeTargetInsets = WindowInsets.imeAnimationTarget
        val navigationInsets = WindowInsets.navigationBars
        var keyboardHasAppeared by remember { mutableStateOf(false) }
        var keyboardEntryFinished by remember { mutableStateOf(false) }

        LaunchedEffect(density) {
            snapshotFlow {
                imeInsets.getBottom(density) > navigationInsets.getBottom(density)
            }.first { it }
            keyboardHasAppeared = true
        }

        LaunchedEffect(density) {
            snapshotFlow {
                val bottom = imeInsets.getBottom(density)
                val target = imeTargetInsets.getBottom(density)
                keyboardHasAppeared && (target <= navigationInsets.getBottom(density) || bottom >= target)
            }.first { it }
            keyboardEntryFinished = true
        }

        val dismiss = {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onDismiss()
        }

        LaunchedEffect(focusRequester, windowInfo) {
            snapshotFlow { inputPlaced && windowInfo.isWindowFocused }
                .first { it }
            focusRequester.requestFocus()
        }

        Box(Modifier.fillMaxSize()) {
            // Dialog supplies the dimmed background; this layer handles outside taps.
            Box(Modifier.matchParentSize().clickable(onClick = dismiss))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime)),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val bottom = imeInsets.getBottom(density)
                            val navigationBottom = navigationInsets.getBottom(density)
                            val target = imeTargetInsets.getBottom(density)
                            val entering = !keyboardEntryFinished && target > bottom && target > navigationBottom
                            val progress = if (entering) {
                                ((bottom - navigationBottom).toFloat() /
                                    (target - navigationBottom)).coerceIn(0f, 1f)
                            } else {
                                1f
                            }
                            // Read in the render phase so visibility and position use this frame's insets.
                            alpha = if (keyboardHasAppeared || bottom > navigationBottom ||
                                hardwareKeyboardAvailable
                            ) 1f else 0f
                            translationY = if (hardwareKeyboardAvailable) 0f else size.height * (1f - progress)
                        },
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(top = 16.dp, bottom = 16.dp),
                    ) {
                        TaskTitleInputField(
                            currentInput = textInput,
                            updateInput = { textInput = it },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onGloballyPositioned {
                                    inputPlaced = it.size.width > 0 && it.size.height > 0
                                },
                        )
                        TaskFuncBtnRow(
                            enableCreate = textInput.isNotBlank(),
                            needLockNotify = needLockNotify,
                            updateLockNotify = { needLockNotify = !needLockNotify },
                            onCreated = {
                                createTaskViewModel.createTask(
                                    Task(title = textInput, needLockNotify = needLockNotify)
                                )
                                dismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTitleInputField(
    currentInput: String,
    updateInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    BasicTextField(
        value = currentInput,
        onValueChange = updateInput,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(TextFieldPadding),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1F)) {
                    if (currentInput.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_task_title_placeholder),
                            style = MaterialTheme.extendedTypography.placeholderLarge
                        )
                    }

                    innerTextField()
                }
            }

        }
    )
}

@Composable
fun TaskFuncBtnRow(
    enableCreate: Boolean,
    needLockNotify: Boolean,
    updateLockNotify: () -> Unit,
    onCreated: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = PageGap),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LockNotifyIconBtnWithTooltip(
            enable = true,
            needLockNotify = needLockNotify,
            updateLockNotify = updateLockNotify
        )

        Button(
            enabled = enableCreate,
            onClick = onCreated
        ) {
            Text(text = stringResource(R.string.create_task))
        }
    }
}
