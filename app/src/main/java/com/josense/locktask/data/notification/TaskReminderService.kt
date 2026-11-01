package com.josense.locktask.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.josense.locktask.R
import com.josense.locktask.notification.TaskNotificationSync
import org.koin.android.ext.android.inject

class TaskReminderService : Service() {
    private val notificationSync: TaskNotificationSync by inject()

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.reminder_service_channel),
                    NotificationManager.IMPORTANCE_LOW,
                )
                    .apply {
                        setSound(null, null)
                        enableVibration(false)
                        setShowBadge(false)
                    },
            )
        }
        val launchIntent = requireNotNull(packageManager.getLaunchIntentForPackage(packageName)).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(getString(R.string.reminder_service_title))
            .setContentText(getString(R.string.reminder_service_description))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    CONTENT_REQUEST_CODE,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .build()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                NO_FOREGROUND_SERVICE_TYPE
            },
        )
        // Recheck persisted tasks after a sticky restart, including when no reminders remain.
        notificationSync.reconcile()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "reminder_service"
        const val NOTIFICATION_ID = 2
        const val CONTENT_REQUEST_CODE = 0
        const val NO_FOREGROUND_SERVICE_TYPE = 0
    }
}
