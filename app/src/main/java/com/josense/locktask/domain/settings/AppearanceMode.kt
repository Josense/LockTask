package com.josense.locktask.domain.settings

enum class AppearanceMode {
    System,
    Light,
    Dark,
    ;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }
}
