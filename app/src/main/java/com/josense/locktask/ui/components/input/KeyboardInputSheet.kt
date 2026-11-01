package com.josense.locktask.ui.components.input

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.josense.locktask.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** 供业务内容指定首次聚焦的输入框和关闭面板，无需处理窗口或软键盘状态。 */
class KeyboardInputSheetScope internal constructor(val initialFocusModifier: Modifier, val dismiss: () -> Unit)

/**
 * 底部对齐、高度随内容调整的输入面板，首次入场与软键盘动画同步。
 *
 * 将 [KeyboardInputSheetScope.initialFocusModifier] 绑定到且仅绑定到一个可编辑输入框。
 * 输入框完成布局且对话框窗口获得焦点后，面板只请求一次输入焦点。
 * 内容可在窗口的可用区域内滚动，不强制占据半屏高度。
 *
 * 调用 [onDismissRequest] 后，调用方必须将此组件移出组合。
 * 再次加入组合时，会开始一次新的入场流程。不支持下拉关闭，也没有独立的退出动画。
 */
@Composable
fun KeyboardInputSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissEnabled: Boolean = true,
    title: String = stringResource(R.string.input_sheet_title),
    content: @Composable KeyboardInputSheetScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (dismissEnabled) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = dismissEnabled,
            dismissOnClickOutside = dismissEnabled,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        InputSheetContent(onDismissRequest, modifier, title, dismissEnabled, content)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputSheetContent(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: String,
    dismissEnabled: Boolean,
    content: @Composable KeyboardInputSheetScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val hardwareKeyboardAvailable = configuration.keyboard != Configuration.KEYBOARD_NOKEYS &&
        configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    val ime = WindowInsets.ime
    val imeTarget = WindowInsets.imeAnimationTarget
    val navigationBars = WindowInsets.navigationBars
    val entry = remember { InputSheetEntryState(hardwareKeyboardAvailable) }
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    var inputPlaced by remember { mutableStateOf(false) }

    val dismiss = {
        if (dismissEnabled) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            currentOnDismiss()
        }
    }
    // 返回键关闭、点击外部关闭，以及调用方移除组件时，都需要释放焦点并收起软键盘。
    DisposableEffect(focusManager, keyboardController) {
        onDispose {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    LaunchedEffect(hardwareKeyboardAvailable) {
        if (hardwareKeyboardAvailable) entry.revealWithoutKeyboard()
    }
    LaunchedEffect(density, ime, imeTarget, navigationBars) {
        snapshotFlow {
            Triple(ime.getBottom(density), imeTarget.getBottom(density), navigationBars.getBottom(density))
        }.collect { (bottom, target, navigationBottom) ->
            entry.onInsetsChanged(bottom, target, navigationBottom)
        }
    }
    LaunchedEffect(focusRequester, windowInfo) {
        snapshotFlow { inputPlaced && windowInfo.isWindowFocused }.first { it }
        if (!focusRequester.requestFocus()) {
            entry.revealWithoutKeyboard()
        } else {
            // 此超时仅用于软键盘无法弹出时显示面板，不用于控制动画时序。
            val keyboardStarted = withTimeoutOrNull(KEYBOARD_START_TIMEOUT_MILLIS) {
                snapshotFlow { entry.hasStarted }.first { it }
            }
            if (keyboardStarted == null) entry.revealWithoutKeyboard()
        }
    }

    val scope = KeyboardInputSheetScope(
        initialFocusModifier = Modifier
            .focusRequester(focusRequester)
            .onGloballyPositioned { inputPlaced = it.size.width > 0 && it.size.height > 0 },
        dismiss = dismiss,
    )
    val dismissLabel = stringResource(R.string.dismiss_input_sheet)
    Box(Modifier.fillMaxSize()) {
        // 对话框负责背景变暗；此层处理面板外部点击，不显示水波纹。
        Box(
            Modifier.matchParentSize()
                .semantics { contentDescription = dismissLabel }
                .clickable(enabled = dismissEnabled, interactionSource = null, indication = null, onClick = dismiss),
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.union(ime)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = modifier.widthIn(max = 640.dp).fillMaxWidth()
                    .semantics {
                        paneTitle = title
                        dismiss {
                            if (dismissEnabled) {
                                dismiss()
                                true
                            } else {
                                false
                            }
                        }
                    }
                    .graphicsLayer {
                        // 在绘制阶段读取当前帧的软键盘占用高度，保持面板入场与键盘动画同步。
                        val presentation = entry.presentation(
                            ime.getBottom(density),
                            imeTarget.getBottom(density),
                            navigationBars.getBottom(density),
                        )
                        alpha = if (presentation.visible) 1f else 0f
                        translationY = size.height * presentation.hiddenFraction
                    },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, bottom = 16.dp),
                ) {
                    scope.content()
                }
            }
        }
    }
}

private const val KEYBOARD_START_TIMEOUT_MILLIS = 1_500L
