package com.josense.locktask.di

import com.josense.locktask.ui.page.create.CreateTaskViewModel
import com.josense.locktask.ui.page.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val taskPresentationModule = module {
    viewModelOf(::CreateTaskViewModel)
    viewModelOf(::HomeViewModel)
    taskDetailModule()
}
