package uk.lobsterdoodle.namepicker.presentation.lists

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.data.ListsData
import uk.lobsterdoodle.namepicker.data.ListsRepository
import uk.lobsterdoodle.namepicker.presentation.BaseViewModel
import uk.lobsterdoodle.namepicker.presentation.ViewState
import javax.inject.Inject

data class ListsViewModelData(
    val lists: List<String>
)

@HiltViewModel
class ListsViewModel @Inject constructor(
    override val scope: CoroutineScope,
    private val repository: ListsRepository
) : BaseViewModel<ListsViewModelData>(
    initialState = ViewState.Loading
) {

    init {
        scope.launch {
            repository.state.collect { data ->
                when (val _data = data) {
                    is ListsData.Loading -> ViewState.Loading
                    is ListsData.Loaded -> {
                        if (_data.payload.isEmpty()) {
                            updateState(
                                ViewState.LoadedNoData
                            )
                        } else {
                            updateState(
                                ViewState.LoadedWithData(
                                    ListsViewModelData(_data.payload)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
