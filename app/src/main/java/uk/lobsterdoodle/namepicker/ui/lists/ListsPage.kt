package uk.lobsterdoodle.namepicker.ui.lists

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme

@Composable
fun ListsPage() {
    NoListsContent()
}

@Preview(showBackground = true)
@Composable
fun GroupsPagePreview() {
    NamesInAHatTheme {
        ListsPage()
    }
}