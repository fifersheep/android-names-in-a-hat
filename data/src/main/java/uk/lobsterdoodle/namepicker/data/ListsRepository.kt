package uk.lobsterdoodle.namepicker.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ListsData {
    object Loading: ListsData()
    data class Loaded(val payload: List<String>): ListsData()
}

class ListsRepository @Inject constructor(
    scope: CoroutineScope
) {
    private val _state = MutableStateFlow<ListsData>(ListsData.Loading)
    val state get() = _state.asStateFlow()

    init {
        scope.launch {
            delay(2_000)
            _state.emit(ListsData.Loaded(listOf("One", "Two", "Three", "Four")))
        }
    }
}