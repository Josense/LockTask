package com.josense.locktask.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.NotificationContract
import com.josense.locktask.domain.notification.TaskNotification
import com.josense.locktask.domain.notification.TaskNotificationGateway
import com.josense.locktask.notification.data.R

class AndroidTaskNotificationGateway(private val context: Context, private val config: NotificationPlatformConfig) :
    TaskNotificationGateway {
    private val manager =
        context.getSystemService(NotificationManager::class.java)
    private val channelId = NotificationContract.CHANNEL_ID

    private companion object {
        const val TAG_PREFIX = "locktask:"
        const val NOTIFICATION_ID = 1
        const val CONTENT_REQUEST_CODE = 0

        const val DISMISS_REQUEST_CODE = 0
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    context.getString(R.string.task_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
                    .apply {
                        setSound(null, null)
                        enableVibration(false)
                        lockscreenVisibility =
                            NotificationCompat.VISIBILITY_PUBLIC
                    },
            )
        }
    }

    override suspend fun access(): NotificationAccess {
        createChannel()
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED ->
                NotificationAccess.PermissionRequired

            NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
                .not() -> NotificationAccess.AppDisabled

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                manager.getNotificationChannel(channelId)?.importance ==
                NotificationManager.IMPORTANCE_NONE ->
                NotificationAccess.ChannelDisabled

            else -> NotificationAccess.Available
        }
    }

    override suspend fun setRetentionEnabled(enabled: Boolean) {
        val intent = Intent(context, TaskReminderService::class.java)
        if (enabled) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }

    override suspend fun publishedKeys(): Set<String> = manager.activeNotifications
        .filter {
            it.id == NOTIFICATION_ID &&
                it.tag?.startsWith(TAG_PREFIX) == true
        }
        .mapTo(mutableSetOf()) { it.tag.removePrefix(TAG_PREFIX) }

    override suspend fun publish(notification: TaskNotification) {
        check(access() == NotificationAccess.Available)
        // Resolve the entry point without a dependency on Presentation classes.
        val intent =
            requireNotNull(
                context.packageManager.getLaunchIntentForPackage(
                    context.packageName,
                ),
            )
                .apply {
                    data =
                        Uri.Builder()
                            .scheme(
                                NotificationContract.URI_SCHEME,
                            )
                            .authority(
                                NotificationContract.TASK_HOST,
                            )
                            .appendPath(notification.taskIds.first().toString())
                            .appendQueryParameter(
                                NotificationContract.NOTIFICATION_KEY,
                                notification.key,
                            )
                            .build()
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                CONTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )
        val dismissIntent = Intent(context, TaskNotificationDismissReceiver::class.java).apply {
            action = TaskNotificationDismissReceiver.ACTION_DISMISSED
            data = Uri.Builder()
                .scheme(NotificationContract.DISMISS_SCHEME)
                .authority(NotificationContract.DISMISS_HOST)
                .appendPath(notification.key).build()
            putExtra(TaskNotificationDismissReceiver.EXTRA_TASK_IDS, notification.taskIds.toIntArray())
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            DISMISS_REQUEST_CODE,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val built =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(config.smallIconRes)
                .setShowWhen(false)
                .setContentTitle(notification.title)
                .setContentText(notification.content)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(
                    contentView(notification, expanded = false),
                )
                .setCustomBigContentView(
                    contentView(notification, expanded = true),
                )
                .setContentIntent(pendingIntent)
                .setDeleteIntent(dismissPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAllowSystemGeneratedContextualActions(false)
                .build()
        manager.notify(TAG_PREFIX + notification.key, NOTIFICATION_ID, built)
    }

    private fun contentView(notification: TaskNotification, expanded: Boolean): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_task).apply {
            setTextViewText(R.id.notification_title, notification.title)
            setTextViewText(R.id.notification_body, notification.content)
            setViewVisibility(
                R.id.notification_body,
                if (expanded && notification.content.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
                },
            )
        }

    override suspend fun cancel(key: String) {
        manager.cancel(TAG_PREFIX + key, NOTIFICATION_ID)
    }
}
