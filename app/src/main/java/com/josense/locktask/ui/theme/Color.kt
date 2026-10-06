package com.josense.locktask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val CustomDisabledLight = Color(0xFFD3D3D3) // 浅色模式下的灰色
val CustomDisabledDark = Color(0xFF4F4F4F)  // 深色模式下的深灰

// 2. 为 ColorScheme 扩展一个属性，根据当前是深色还是浅色主题自动返回对应的颜色值
val ColorScheme.customDisabled: Color
    @Composable
    get() = if (isSystemInDarkTheme()) CustomDisabledDark else CustomDisabledLight