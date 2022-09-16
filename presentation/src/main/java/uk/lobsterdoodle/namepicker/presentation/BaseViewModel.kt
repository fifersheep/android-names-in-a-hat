package uk.lobsterdoodle.namepicker.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ViewState<out T> {
    object Loading: ViewState<Nothing>()
    data class Loaded<T>(val data: T): ViewState<T>()
}

abstract class BaseViewModel<S>(
    initialState: ViewState<S>
): ViewModel() {
    abstract val scope: CoroutineScope

    private val _state = MutableStateFlow(initialState)
    val state get() = _state.asStateFlow()

    protected fun updateState(state: ViewState<S>) {
        scope.launch {
            _state.emit(state)
        }
    }
}
