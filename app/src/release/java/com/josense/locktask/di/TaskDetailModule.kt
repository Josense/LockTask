package com.josense.locktask.di

import com.josense.locktask.ui.page.task_detail.TaskDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

internal fun Module.taskDetailModule() {
    viewModelOf(::TaskDetailViewModel)
}
