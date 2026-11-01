package com.josense.locktask.ui.page

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.page.create.CreateTaskAction
import com.josense.locktask.ui.page.create.CreateTaskViewModel
import com.josense.locktask.ui.page.create.Submission
import com.josense.locktask.ui.page.home.HomeAction
import com.josense.locktask.ui.page.home.HomeListState
import com.josense.locktask.ui.page.home.HomeViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskCreationFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRepository()
    private lateinit var create: CreateTaskViewModel
    private lateinit var home: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        create = CreateTaskViewModel(repository)
        home = HomeViewModel(repository)
        create.onAction(CreateTaskAction.TitleChanged("New task"))
    }

    @After
    fun tearDown() {
        create.viewModelScope.cancel()
        home.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `successful session is cleared on close and a new session starts fresh`() = runTest(dispatcher) {
        val store = ViewModelStore()
        store.put("create", create)
        create.onAction(CreateTaskAction.Submit)
        runCurrent()
        assertEquals(Submission.Saving, create.state.value.submission)
        repository.result.complete(42)
        runCurrent()
        assertEquals(Submission.Succeeded(42), create.state.value.submission)
        store.clear()
        assertFalse(create.viewModelScope.coroutineContext[kotlinx.coroutines.Job]!!.isActive)

        val next = CreateTaskViewModel(repository)
        val nextStore = ViewModelStore()
        nextStore.put("create", next)
        assertEquals("", next.state.value.title)
        assertEquals(Submission.Idle, next.state.value.submission)
        assertTrue(next.state.value.needLockNotify)
        nextStore.clear()
    }

    @Test
    fun `duplicate submit is ignored and submitted input remains stable`() = runTest(dispatcher) {
        create.onAction(CreateTaskAction.ToggleLockNotify)
        create.onAction(CreateTaskAction.Submit)
        create.onAction(CreateTaskAction.Submit)
        create.onAction(CreateTaskAction.TitleChanged("Changed"))
        create.onAction(CreateTaskAction.ToggleLockNotify)
        runCurrent()
        assertEquals(1, repository.insertCount)
        assertEquals("New task", repository.inserted?.title)
        assertEquals(false, repository.inserted?.needLockNotify)
        repository.result.complete(7)
        runCurrent()
        create.onAction(CreateTaskAction.Submit)
        runCurrent()
        assertEquals(1, repository.insertCount)
    }

    @Test
    fun `failure preserves draft and supports retry`() = runTest(dispatcher) {
        repository.result.completeExceptionally(IllegalStateException("Write failed"))
        create.onAction(CreateTaskAction.Submit)
        runCurrent()
        assertEquals(Submission.Failed, create.state.value.submission)
        assertEquals("New task", create.state.value.title)
        repository.result = CompletableDeferred(8)
        create.onAction(CreateTaskAction.Submit)
        runCurrent()
        assertEquals(Submission.Succeeded(8), create.state.value.submission)
    }

    @Test
    fun `blank input cannot submit`() = runTest(dispatcher) {
        create.onAction(CreateTaskAction.TitleChanged("   "))
        create.onAction(CreateTaskAction.Submit)
        runCurrent()
        assertEquals(0, repository.insertCount)
        assertEquals(Submission.Idle, create.state.value.submission)
    }

    @Test
    fun `home retains requested id across delayed list update until acknowledgement`() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        home.onAction(HomeAction.AnchorTask(42))
        runCurrent()
        assertEquals(HomeListState.Empty, home.state.value.list)
        assertEquals(42, home.state.value.pendingAnchorTaskId)
        repository.tasks.value = listOf(Task(id = 1, title = "Other"), Task(id = 42, title = "New task"))
        runCurrent()
        assertEquals(42, home.state.value.pendingAnchorTaskId)
        assertEquals(42, (home.state.value.list as HomeListState.Content).tasks[1].id)
        home.onAction(HomeAction.AnchorTask(43))
        home.onAction(HomeAction.AnchorHandled(42))
        runCurrent()
        assertEquals(43, home.state.value.pendingAnchorTaskId)
        home.onAction(HomeAction.AnchorHandled(43))
        runCurrent()
        assertNull(home.state.value.pendingAnchorTaskId)
    }

    @Test
    fun `repository removal retains original order until animation is acknowledged`() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        val tasks = (1..3).map { Task(id = it, title = "Task $it") }
        repository.tasks.value = tasks
        runCurrent()
        repository.tasks.value = listOf(tasks[0], tasks[2])
        runCurrent()
        assertEquals(tasks, (home.state.value.list as HomeListState.Content).tasks)
        assertEquals(setOf(2), home.state.value.removingTaskIds)
        home.onAction(HomeAction.RemovalFinished(setOf(2)))
        runCurrent()
        assertEquals(listOf(tasks[0], tasks[2]), (home.state.value.list as HomeListState.Content).tasks)
        assertTrue(home.state.value.removingTaskIds.isEmpty())
    }

    @Test
    fun `last row stays until removal animation finishes`() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        val task = Task(id = 1, title = "Last")
        repository.tasks.value = listOf(task)
        runCurrent()
        repository.tasks.value = emptyList()
        runCurrent()
        assertEquals(listOf(task), (home.state.value.list as HomeListState.Content).tasks)
        assertEquals(setOf(1), home.state.value.removingTaskIds)
        home.onAction(HomeAction.RemovalFinished(setOf(1)))
        runCurrent()
        assertEquals(HomeListState.Empty, home.state.value.list)
    }

    @Test
    fun `background updates preserve pending rows and ignore stale removal acknowledgement`() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        val tasks = (1..4).map { Task(id = it, title = "Task $it") }
        repository.tasks.value = tasks
        runCurrent()
        repository.tasks.value = listOf(tasks[0], tasks[3])
        runCurrent()
        assertEquals(tasks, (home.state.value.list as HomeListState.Content).tasks)
        assertEquals(setOf(2, 3), home.state.value.removingTaskIds)
        // A task restored before its exit completes must survive the old callback.
        repository.tasks.value = listOf(tasks[0], tasks[1], tasks[3])
        runCurrent()
        home.onAction(HomeAction.RemovalFinished(setOf(2, 3)))
        runCurrent()
        assertEquals(listOf(tasks[0], tasks[1], tasks[3]), (home.state.value.list as HomeListState.Content).tasks)
    }

    @Test
    fun `home starts loading on subscription and retains rows across a long detail visit`() = runTest(dispatcher) {
        runCurrent()
        assertEquals(0, repository.activeSubscriptions)
        assertEquals(HomeListState.Loading, home.state.value.list)
        val task = Task(id = 1, title = "Task")
        repository.tasks.value = listOf(task)
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        runCurrent()
        assertEquals(1, repository.activeSubscriptions)
        collector.cancel()
        advanceTimeBy(6000)
        runCurrent()
        repository.tasks.value = emptyList()
        runCurrent()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        runCurrent()
        assertEquals(2, repository.activeSubscriptions)
        assertEquals(listOf(task), (home.state.value.list as HomeListState.Content).tasks)
        assertEquals(setOf(1), home.state.value.removingTaskIds)
    }

    @Test
    fun `removal notification is sent once after animation and undo preserves current edits`() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { home.state.collect {} }
        val notifications = mutableListOf<Task>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            home.removals.collect { notifications.add(it) }
        }
        val original = Task(id = 1, title = "Original")
        repository.tasks.value = listOf(original)
        runCurrent()
        repository.storedTasks[1] = original.copy(title = "Edited", isCompleted = true)
        repository.tasks.value = emptyList()
        runCurrent()
        assertTrue(notifications.isEmpty())
        home.onAction(HomeAction.RemovalFinished(setOf(1)))
        runCurrent()
        assertEquals(listOf(original), notifications)
        home.onAction(HomeAction.RemovalFinished(setOf(1)))
        runCurrent()
        assertEquals(1, notifications.size)
        home.onAction(HomeAction.UndoRemoval(notifications.single()))
        runCurrent()
        assertEquals(1, repository.updateCount)
        assertEquals("Edited", repository.storedTasks[1]?.title)
        assertEquals(false, repository.storedTasks[1]?.isCompleted)
        assertEquals(1, (home.state.value.list as HomeListState.Content).tasks.single().id)
    }

    private class FakeRepository : TaskRepository {
        val tasks = MutableStateFlow<List<Task>>(emptyList())
        var result = CompletableDeferred<Int>()
        var insertCount = 0
        var inserted: Task? = null
        var activeSubscriptions = 0
        val storedTasks = mutableMapOf<Int, Task>()
        var updateCount = 0
        override fun getAllTasks() = tasks
        override fun getAllActiveTasks() = tasks.onStart { activeSubscriptions++ }
        override fun getTaskById(taskId: Int) = flowOf(storedTasks[taskId])
        override suspend fun insertTask(task: Task): Int {
            insertCount++
            inserted = task
            return result.await()
        }
        override suspend fun updateTask(task: Task) {
            updateCount++
            storedTasks[task.id] = task
            tasks.value = storedTasks.values.filter { !it.isCompleted && !it.isDeleted }
        }
        override suspend fun completeTask(task: Task, isCompleted: Boolean) = Unit
        override suspend fun deleteTaskSoftly(task: Task) = Unit
        override suspend fun clearTrash() = Unit
    }
}
