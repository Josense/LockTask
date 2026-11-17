package com.josense.locktask.di

import com.josense.locktask.debug.DetailSaveFailure
import com.josense.locktask.debug.FailingDetailRepository
import com.josense.locktask.ui.page.task_detail.TaskDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.taskDetailModule() {
    single { DetailSaveFailure() }
    viewModel {
        TaskDetailViewModel(FailingDetailRepository(get(), get()), get())
    }
}
