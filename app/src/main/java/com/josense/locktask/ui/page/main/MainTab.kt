package com.josense.locktask.ui.page.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.josense.locktask.R

internal enum class MainTab(@param:StringRes val label: Int, val icon: ImageVector) {
    Tasks(R.string.tab_tasks, Icons.Outlined.Checklist),
    Templates(R.string.tab_templates, Icons.Outlined.DashboardCustomize),
    Settings(R.string.tab_settings, Icons.Outlined.Settings),
}
