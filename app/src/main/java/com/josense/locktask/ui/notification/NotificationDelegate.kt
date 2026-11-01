package com.josense.locktask.ui.notification

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.josense.locktask.R
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.TaskNotificationGateway
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationDelegate(
    private val activity: ComponentActivity,
    private val gateway: TaskNotificationGateway,
    private val repository: TaskRepository,
    private val reconcile: () -> Unit,
) {
    internal data class Target(val requestId: Long, val taskId: Int)

    internal data class State(
        val target: Target? = null,
        val access: NotificationAccess = NotificationAccess.Available,
        val backgroundGuideVisible: Boolean = false,
        val backgroundNoticeDismissed: Boolean = false,
    )

    internal sealed interface Action {
        data object Refresh : Action

        data object Enable : Action

        data object ShowBackgroundGuide : Action

        data object HideBackgroundGuide : Action

        data object DismissBackgroundNotice : Action

        data object OpenBackgroundSettings : Action

        data class Created(val taskId: Int) : Action

        data class Consume(val requestId: Long) : Action
    }

    private val preferences = activity.getPreferences(Context.MODE_PRIVATE)

    internal var state by mutableStateOf(
        State(backgroundNoticeDismissed = preferences.getBoolean(BACKGROUND_NOTICE_DISMISSED_KEY, false)),
    )
        private set

    private val permission =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) {
            onAction(Action.Refresh)
        }

    init {
        val restored =
            activity.savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE)
        if (restored == null) {
            acceptIntent(activity.intent)
        } else {
            restored
                .getInt(TARGET_TASK_KEY, 0)
                .takeIf { it > 0 }
                ?.let { acceptTask(it) }
        }
        activity.savedStateRegistry.registerSavedStateProvider(SAVED_STATE) {
            Bundle().apply {
                state.target?.let { putInt(TARGET_TASK_KEY, it.taskId) }
            }
        }
        activity.addOnNewIntentListener { acceptIntent(it) }
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    onAction(Action.Refresh)
                }
            },
        )
    }

    private fun acceptIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (
            uri.scheme !=
            activity.getString(R.string.notification_uri_scheme) ||
            uri.host != activity.getString(R.string.notification_uri_host)
        ) {
            return
        }
        uri.lastPathSegment?.toIntOrNull()?.takeIf { it > 0 }?.let(::acceptTask)
    }

    private fun acceptTask(taskId: Int) {
        state = state.copy(target = Target(System.nanoTime(), taskId))
    }

    internal fun onAction(action: Action) {
        when (action) {
            Action.Refresh -> {
                reconcile()
                activity.lifecycleScope.launch {
                    state = state.copy(access = gateway.access())
                }
            }
            Action.Enable -> enable(automatic = false)
            Action.ShowBackgroundGuide -> state = state.copy(backgroundGuideVisible = true)
            Action.HideBackgroundGuide -> state = state.copy(backgroundGuideVisible = false)
            Action.DismissBackgroundNotice -> {
                preferences.edit { putBoolean(BACKGROUND_NOTICE_DISMISSED_KEY, true) }
                state = state.copy(backgroundNoticeDismissed = true)
            }
            Action.OpenBackgroundSettings -> openBackgroundSettings()
            is Action.Created -> {
                activity.lifecycleScope.launch {
                    if (
                        repository
                            .getTaskById(action.taskId)
                            .first()
                            ?.needLockNotify == true
                    ) {
                        enable(automatic = true)
                    }
                }
            }
            is Action.Consume ->
                if (state.target?.requestId == action.requestId) {
                    state = state.copy(target = null)
                }
        }
    }

    private fun openBackgroundSettings() {
        // OEM manual-management authorization has no portable read API. Never mark it granted here.
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${activity.packageName}".toUri(),
        )
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun enable(automatic: Boolean) {
        activity.lifecycleScope.launch {
            val access = gateway.access()
            state = state.copy(access = access)
            if (
                access == NotificationAccess.PermissionRequired &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                val asked = preferences.getBoolean(PERMISSION_ASKED_KEY, false)
                if (
                    asked.not() ||
                    (
                        automatic.not() &&
                            activity.shouldShowRequestPermissionRationale(
                                Manifest.permission.POST_NOTIFICATIONS,
                            )
                        )
                ) {
                    preferences.edit { putBoolean(PERMISSION_ASKED_KEY, true) }
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@launch
                }
            }
            if (automatic || access == NotificationAccess.Available) {
                return@launch
            }
            val channelId = activity.getString(R.string.notification_channel_id)
            val settings =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(
                        if (access == NotificationAccess.ChannelDisabled) {
                            Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                        } else {
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        },
                    )
                        .putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            activity.packageName,
                        )
                        .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                } else {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:${activity.packageName}".toUri(),
                    )
                }
            activity.startActivity(settings)
        }
    }

    private companion object {
        const val BACKGROUND_NOTICE_DISMISSED_KEY = "background_notice_dismissed"
        const val SAVED_STATE = "task_notification_delegate"
        const val TARGET_TASK_KEY = "task"
        const val PERMISSION_ASKED_KEY = "notification_permission_asked"
    }
}
