package uk.lobsterdoodle.namepicker.ui

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lists hidden by a delayed delete (spec D12) whose delete hasn't been committed yet. Other screens
 * read it so they don't write to a list the user has just deleted: Edit list skips its
 * rename-on-leave, so Undo brings the list back with its old name.
 */
@Singleton
class PendingDeletes @Inject constructor() {
    private val _ids = MutableStateFlow(emptySet<Long>())
    val ids: StateFlow<Set<Long>> = _ids.asStateFlow()

    fun add(id: Long) = _ids.update { it + id }

    fun remove(id: Long) = _ids.update { it - id }

    operator fun contains(id: Long): Boolean = id in _ids.value
}
