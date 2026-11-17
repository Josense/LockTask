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
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.NotificationAction
import com.josense.locktask.domain.notification.NotificationContract
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.domain.notification.NotificationReconciler
import com.josense.locktask.domain.notification.NotificationReminderLookup
import com.josense.locktask.domain.notification.NotificationTarget
import com.josense.locktask.domain.notification.NotificationUiState
import com.josense.locktask.domain.notification.TaskNotificationGateway
import kotlinx.coroutines.launch

internal class NotificationDelegate(
    private val activity: ComponentActivity,
    private val gateway: TaskNotificationGateway,
    private val reminderLookup: NotificationReminderLookup,
    private val reconciler: NotificationReconciler,
) : NotificationController {
    private val preferences = activity.getPreferences(Context.MODE_PRIVATE)

    override var state by mutableStateOf(
        NotificationUiState(backgroundNoticeDismissed = preferences.getBoolean(BACKGROUND_NOTICE_DISMISSED_KEY, false)),
    )
        private set

    private val permission =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) {
            onAction(NotificationAction.Refresh)
        }

    init {
        val restored =
            activity.savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE)
        if (restored == null) {
            acceptIntent(activity.intent)
        } else if (restored.getBoolean(CREATE_TASK_KEY)) {
            acceptCreate()
        } else {
            restored
                .getInt(TARGET_TASK_KEY, 0)
                .takeIf { it > 0 }
                ?.let { acceptTask(it) }
        }
        activity.savedStateRegistry.registerSavedStateProvider(SAVED_STATE) {
            Bundle().apply {
                when (val target = state.target) {
                    is NotificationTarget.Task -> putInt(TARGET_TASK_KEY, target.taskId)
                    is NotificationTarget.Create -> putBoolean(CREATE_TASK_KEY, true)
                    null -> Unit
                }
            }
        }
        activity.addOnNewIntentListener { acceptIntent(it) }
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    onAction(NotificationAction.Refresh)
                }
            },
        )
    }

    private fun acceptIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != NotificationContract.URI_SCHEME) return
        when (uri.host) {
            NotificationContract.TASK_HOST ->
                uri.lastPathSegment?.toIntOrNull()?.takeIf { it > 0 }?.let(::acceptTask)
            NotificationContract.CREATE_HOST -> acceptCreate()
        }
    }

    private fun acceptTask(taskId: Int) {
        state = state.copy(target = NotificationTarget.Task(System.nanoTime(), taskId), backgroundGuideVisible = false)
    }

    private fun acceptCreate() {
        state = state.copy(target = NotificationTarget.Create(System.nanoTime()), backgroundGuideVisible = false)
    }

    override fun onAction(action: NotificationAction) {
        when (action) {
            NotificationAction.Refresh -> {
                reconciler.reconcile()
                activity.lifecycleScope.launch {
                    state = state.copy(access = gateway.access())
                }
            }
            NotificationAction.Enable -> enable(automatic = false)
            NotificationAction.ShowBackgroundGuide -> state = state.copy(backgroundGuideVisible = true)
            NotificationAction.HideBackgroundGuide -> state = state.copy(backgroundGuideVisible = false)
            NotificationAction.DismissBackgroundNotice -> {
                preferences.edit { putBoolean(BACKGROUND_NOTICE_DISMISSED_KEY, true) }
                state = state.copy(backgroundNoticeDismissed = true)
            }
            NotificationAction.OpenBackgroundSettings -> openBackgroundSettings()
            NotificationAction.OpenNotificationSettings -> openNotificationSettings(state.access)
            is NotificationAction.Created -> {
                activity.lifecycleScope.launch {
                    if (
                        reminderLookup.needsReminder(action.taskId)
                    ) {
                        enable(automatic = true)
                    }
                }
            }
            is NotificationAction.Consume ->
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
            openNotificationSettings(access)
        }
    }

    private fun openNotificationSettings(access: NotificationAccess) {
        val channelId = NotificationContract.CHANNEL_ID
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

    private companion object {
        const val BACKGROUND_NOTICE_DISMISSED_KEY = "background_notice_dismissed"
        const val SAVED_STATE = "task_notification_delegate"
        const val CREATE_TASK_KEY = "create_task"
        const val TARGET_TASK_KEY = "task"
        const val PERMISSION_ASKED_KEY = "notification_permission_asked"
    }
}
