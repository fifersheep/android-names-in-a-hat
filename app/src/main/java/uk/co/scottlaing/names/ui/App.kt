package uk.co.scottlaing.names.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.scottlaing.names.ui.common.Greeting
import uk.co.scottlaing.names.ui.theme.NamesTheme

@Composable
fun App() {
    NamesTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Greeting("Android")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    NamesTheme {
        App()
    }
}
