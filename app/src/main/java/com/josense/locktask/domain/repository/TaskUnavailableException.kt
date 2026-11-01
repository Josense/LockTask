package com.josense.locktask.domain.repository

/** The task was removed or moved to trash before the edit could be saved. */
class TaskUnavailableException : IllegalStateException("Task no longer exists or is deleted")
