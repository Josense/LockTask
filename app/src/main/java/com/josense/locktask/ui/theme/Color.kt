package com.josense.locktask.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// App icon colors, reused by the light and dark Material 3 schemes.
val AppLight = Color(0xFFF4F7F4)
val AppDark = Color(0xFF202820)

val CustomDisabledLight = Color(0xFFD3D3D3) // 浅色模式下的灰色
val CustomDisabledDark = Color(0xFF4F4F4F)  // 深色模式下的深灰

// 2. 为 ColorScheme 扩展一个属性，根据当前是深色还是浅色主题自动返回对应的颜色值
val ColorScheme.customDisabled: Color
    @Composable
    get() = if (surface == AppDark) CustomDisabledDark else CustomDisabledLight
