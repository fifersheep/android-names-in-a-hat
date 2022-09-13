package uk.lobsterdoodle.namepicker.ui.lists

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.ui.common.CallToAction
import uk.lobsterdoodle.namepicker.ui.common.CallToActionType
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme

@Composable
fun NoListsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Start by creating a list to organise your names. You'll be able to manage multiple lists to create draws from.",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(
                start = 64.dp,
                end = 64.dp,
                bottom = 16.dp,
            )
        )
        CallToAction(
            CallToActionType.Primary,
            label = "Create List",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoGroupsContentPreview() {
    NamesInAHatTheme {
        NoListsContent()
    }
}
