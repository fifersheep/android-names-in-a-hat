package uk.lobsterdoodle.namepicker.presentation.lists

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.presentation.BaseViewModel
import uk.lobsterdoodle.namepicker.presentation.ViewState
import javax.inject.Inject

data class ListsViewModelData(
    val lists: List<String>
)

@HiltViewModel
class ListsViewModel @Inject constructor(
    override val scope: CoroutineScope
) : BaseViewModel<ListsViewModelData>(
    initialState = ViewState.Loading
) {
    fun loadLists() {
        scope.launch {
            delay(2_000)
            updateState(
                ViewState.Loaded(
                    ListsViewModelData(
                        listOf("One", "Two")
                    )
                )
            )
        }
    }
}