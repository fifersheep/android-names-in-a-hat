package uk.lobsterdoodle.namepicker.ui.editlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.di.AppScope
import uk.lobsterdoodle.namepicker.shared.repository.GroupRepository
import uk.lobsterdoodle.namepicker.shared.usecase.GroupEditor
import uk.lobsterdoodle.namepicker.shared.usecase.WriteResult
import uk.lobsterdoodle.namepicker.shared.usecase.validateLabel
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.PendingDeletes
import uk.lobsterdoodle.namepicker.ui.status

/**
 * Edit list's list name (spec "Edit list", "Navigation" rules 1–3, D15). One ViewModel per visit:
 * the shell keeps it when it swaps `EditList(null)` for `EditList(id)`.
 *
 * The name is saved when it is **committed**: IME Next ([commitName]), Done ([done]), or leaving
 * an existing list ([onCleared]). A new list is created on its first commit; leaving before that
 * creates nothing. A blank name is never saved: committing it shows the error, and leaving with it
 * keeps the old name. Validation and trimming are `:shared`'s ([GroupEditor], `validateLabel`).
 *
 * The field's text and error are snapshot state backed by [SavedStateHandle], so a `TextField` can
 * edit them directly and they survive rotation and process death.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = EditListViewModel.Factory::class)
class EditListViewModel @AssistedInject constructor(
    @Assisted listId: Long?,
    groups: GroupRepository,
    private val editor: GroupEditor,
    private val pendingDeletes: PendingDeletes,
    @param:AppScope private val appScope: CoroutineScope,
    private val saved: SavedStateHandle,
) : ViewModel() {

    /** The list being edited: from the key, or from saved state once this visit has created it. */
    private val id = MutableStateFlow(saved.get<Long>(KEY_ID) ?: listId)
    private var creating = false

    /** The name as last stored, so leaving doesn't rewrite an unchanged one. */
    private var storedLabel: String? = null

    var text: String by mutableStateOf(saved.get<String>(KEY_TEXT).orEmpty())
        private set

    /** The "Enter a list name" error is showing. */
    var error: Boolean by mutableStateOf(saved.get<Boolean>(KEY_ERROR) ?: false)
        private set

    val listExists: Boolean get() = id.value != null

    /** The list's id: null until this visit creates it. The names section follows this, not the key (NAM-8 review A3). */
    val listId: StateFlow<Long?> = id.asStateFlow()

    val status: StateFlow<ListStatus> = id
        .flatMapLatest { id -> if (id == null) flowOf(ListStatus.Unsaved) else groups.status(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, if (id.value == null) ListStatus.Unsaved else ListStatus.Loading)

    init {
        viewModelScope.launch {
            status.collect { status ->
                if (status !is ListStatus.Present) return@collect
                storedLabel = status.label
                // An existing list opens with its name, once per visit; after that the field is the user's.
                if (saved.get<Boolean>(KEY_PREFILLED) != true) {
                    saved[KEY_PREFILLED] = true
                    storeText(status.label)
                }
            }
        }
    }

    fun onTextChange(value: String) {
        storeText(value)
        storeError(false)
    }

    /**
     * Commits the name (IME Next). Creates the list on the first commit and calls [onCreated] with
     * its id; renames it after that. Blank shows the error.
     *
     * @return false if the name was blank, so the caller keeps focus here; true otherwise (Next then
     *   moves on to the add field).
     */
    fun commitName(onCreated: (Long) -> Unit): Boolean {
        val label = validateLabel(text)
        if (label == null) {
            storeError(true)
            return false
        }
        val existing = id.value
        if (existing != null) {
            rename(existing, label)
            return true
        }
        if (creating) return true
        creating = true
        viewModelScope.launch {
            when (val result = editor.create(label)) {
                is WriteResult.Saved -> {
                    storedLabel = label
                    saved[KEY_ID] = result.value
                    saved[KEY_PREFILLED] = true
                    id.value = result.value
                    storeText(label)
                    onCreated(result.value)
                }
                WriteResult.Blank -> storeError(true)
            }
            creating = false
        }
        return true
    }

    /**
     * Focus moved from the name to another field (spec: "focus moving to another field with
     * non-blank text"): renames an existing list if the name is non-blank and changed. Never shows
     * the error and never creates: the names section's fields only exist once the list does.
     */
    fun commitNameOnFocusLeave() {
        val existing = id.value ?: return
        val label = validateLabel(text) ?: return
        rename(existing, label)
    }

    /** Done: saves a changed name, then calls [onDone]. Blank shows the error and stays. */
    fun done(onDone: () -> Unit) {
        val existing = id.value ?: return
        val label = validateLabel(text) ?: return storeError(true)
        rename(existing, label)
        onDone()
    }

    /** Leaving an existing list saves a non-blank change, unless the list is being deleted. */
    override fun onCleared() {
        val existing = id.value ?: return
        if (existing in pendingDeletes || status.value is ListStatus.Gone) return
        val label = validateLabel(text) ?: return
        if (label == storedLabel) return
        appScope.launch { editor.rename(existing, label) }
    }

    private fun rename(id: Long, label: String) {
        storeText(label)
        if (label == storedLabel) return
        storedLabel = label
        // The app scope, not viewModelScope: Done pops the screen at once, and a cancelled write
        // would be lost because onCleared sees the name as already stored.
        appScope.launch { editor.rename(id, label) }
    }

    private fun storeText(value: String) {
        text = value
        saved[KEY_TEXT] = value
    }

    private fun storeError(value: Boolean) {
        error = value
        saved[KEY_ERROR] = value
    }

    @AssistedFactory
    interface Factory {
        fun create(listId: Long?): EditListViewModel
    }

    private companion object {
        const val KEY_ID = "listId"
        const val KEY_TEXT = "listName"
        const val KEY_ERROR = "listNameError"
        const val KEY_PREFILLED = "listNamePrefilled"
    }
}
