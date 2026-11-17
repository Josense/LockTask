package com.josense.locktask.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.josense.locktask.domain.notification.NotificationDismissalHandler
import com.josense.locktask.notification.data.di.notificationScopeQualifier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TaskNotificationDismissReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val handler: NotificationDismissalHandler by inject()
    private val scope: CoroutineScope by inject(notificationScopeQualifier)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISSED) return
        val ids = intent.getIntArrayExtra(EXTRA_TASK_IDS)?.toList().orEmpty()
        if (ids.isEmpty()) return
        val pending = goAsync()
        scope.launch {
            try {
                withTimeout(DISMISSAL_TIMEOUT) { handler.onDismissed(ids) }
            } catch (error: Exception) {
                Log.w(LOG_TAG, DISMISSAL_FAILURE_MESSAGE, error)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        // Leave time to finish the pending broadcast before the system's short receiver deadline.
        private val DISMISSAL_TIMEOUT = 8.seconds
        private const val LOG_TAG = "NotificationDismiss"
        private const val DISMISSAL_FAILURE_MESSAGE = "Unable to record notification dismissal"

        const val ACTION_DISMISSED = "com.josense.locktask.NOTIFICATION_DISMISSED"
        const val EXTRA_TASK_IDS = "task_ids"
    }
}
