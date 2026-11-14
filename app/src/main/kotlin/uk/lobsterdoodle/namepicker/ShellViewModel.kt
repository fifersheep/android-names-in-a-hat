package uk.lobsterdoodle.namepicker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import uk.lobsterdoodle.namepicker.navigation.AppKey
import uk.lobsterdoodle.namepicker.navigation.launchStack
import uk.lobsterdoodle.namepicker.shared.repository.PreferencesRepository
import uk.lobsterdoodle.namepicker.shared.usecase.ActiveGroup

/**
 * The shell's state: the launch route, and the active-list writes the back stack triggers
 * (spec "Launch and the active list").
 */
@HiltViewModel
class ShellViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val activeGroup: ActiveGroup,
) : ViewModel() {

    private val _start = MutableStateFlow<List<AppKey>?>(null)

    /**
     * The stack to launch with, or null until it is known. The splash stays up while it is null.
     * Routing reads only the settings, never the database: a list that has since gone is caught
     * by the destination, which then calls [onListGone].
     */
    val start: StateFlow<List<AppKey>?> = _start.asStateFlow()

    init {
        viewModelScope.launch { _start.value = launchStack(readActiveListId()) }
    }

    /** An explicit back-out from Draw to Lists on a phone: the list is no longer active. */
    fun onLeftList() {
        viewModelScope.launch { activeGroup.close() }
    }

    /** The list [id] no longer exists: forget it, unless another list became active meanwhile. */
    fun onListGone(id: Long) {
        viewModelScope.launch { prefs.clearActiveGroupIf(id) }
    }

    private suspend fun readActiveListId(): Long? = try {
        prefs.activeGroupId.first()
    } catch (e: IOException) {
        // Corruption is already replaced with empty settings (NAM-6); this is a real read failure.
        Log.w(TAG, "Couldn't read the active list; opening Lists", e)
        null
    }

    private companion object {
        const val TAG = "ShellViewModel"
    }
}
