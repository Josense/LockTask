package com.josense.locktask.debug

import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DetailSaveFailure {
    private val mutableArmed = MutableStateFlow(false)
    val armed = mutableArmed.asStateFlow()

    fun toggle() {
        mutableArmed.value = mutableArmed.value.not()
    }

    fun check() {
        if (mutableArmed.compareAndSet(true, false)) {
            throw IOException("Debug: simulated task save failure")
        }
    }
}

internal class FailingDetailRepository(private val delegate: TaskRepository, private val failure: DetailSaveFailure) :
    TaskRepository by delegate {
    override suspend fun updateTask(task: Task) {
        failure.check()
        delegate.updateTask(task)
    }

    override suspend fun updateTaskText(taskId: Int, title: String, content: String) {
        failure.check()
        delegate.updateTaskText(taskId, title, content)
    }

    override suspend fun completeTask(task: Task, isCompleted: Boolean) {
        failure.check()
        delegate.completeTask(task, isCompleted)
    }
    override suspend fun deleteTaskSoftly(task: Task) {
        failure.check()
        delegate.deleteTaskSoftly(task)
    }
}
