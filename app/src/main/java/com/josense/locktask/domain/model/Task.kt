package com.josense.locktask.domain.model


import java.util.Date

data class Task(
    val id: Int = 0,
    val title: String,
    val content: String = "",
    val needLockNotify: Boolean = true,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
)