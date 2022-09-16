package uk.lobsterdoodle.namepicker.ui.lists

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import uk.lobsterdoodle.namepicker.presentation.lists.ListsViewModel
import uk.lobsterdoodle.namepicker.presentation.ViewState
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme

@Composable
fun ListsPage(
    viewModel: ListsViewModel = hiltViewModel<ListsViewModel>(),
) {
    val state by viewModel.state.collectAsState()

    when (val _state = state) {
        is ViewState.Loading -> Text(text = "Loading")
        is ViewState.Loaded -> if (_state.data.lists.isEmpty()) NoListsContent() else Text(text = _state.data.lists.joinToString())
    }

    viewModel.loadLists()
}

@Preview(showBackground = true)
@Composable
fun ListsPagePreview() {
    NamesInAHatTheme {
        ListsPage()
    }
}