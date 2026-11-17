package com.josense.locktask.ui.page.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.notification.NotificationAction
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.ui.notification.AccessNotice
import com.josense.locktask.ui.notification.HomeEffect
import com.josense.locktask.ui.notification.taskCreated
import com.josense.locktask.ui.page.home.HomePageRoot
import com.josense.locktask.ui.page.settings.SettingsPageRoot
import com.josense.locktask.ui.page.templates.TemplatesPageRoot

@Composable
fun MainPageRoot(notifications: NotificationController, onTaskClick: (Task) -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Tasks) }
    val tabStates = rememberSaveableStateHolder()
    LaunchedEffect(notifications.state.target?.requestId) {
        if (notifications.state.target != null) selectedTab = MainTab.Tasks
    }
    BackHandler(enabled = selectedTab != MainTab.Tasks) { selectedTab = MainTab.Tasks }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        modifier = Modifier.testTag("main_tab_${tab.name}"),
                        selected = selectedTab == tab,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            tabStates.SaveableStateProvider(selectedTab) {
                when (selectedTab) {
                    MainTab.Tasks -> HomePageRoot(
                        onTaskClick = onTaskClick,
                        notificationEffects = { state, dispatch, create ->
                            notifications.HomeEffect(state, dispatch, create)
                        },
                        notificationNotice = { state -> notifications.AccessNotice(state) },
                        onTaskCreated = notifications::taskCreated,
                    )
                    MainTab.Templates -> TemplatesPageRoot()
                    MainTab.Settings -> SettingsPageRoot(
                        notificationAccess = notifications.state.access,
                        onNotificationSettings = {
                            notifications.onAction(NotificationAction.OpenNotificationSettings)
                        },
                        onBackgroundSettings = {
                            notifications.onAction(NotificationAction.ShowBackgroundGuide)
                        },
                    )
                }
            }
        }
    }
}
