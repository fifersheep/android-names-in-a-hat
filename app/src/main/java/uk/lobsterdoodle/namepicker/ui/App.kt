package uk.lobsterdoodle.namepicker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.lobsterdoodle.namepicker.ui.groups.GroupsPage
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme

@Composable
fun App() {
    NamesInAHatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            GroupsPage()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    App()
}
