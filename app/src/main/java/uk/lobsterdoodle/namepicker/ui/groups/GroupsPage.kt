package uk.lobsterdoodle.namepicker.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.ui.common.CallToAction
import uk.lobsterdoodle.namepicker.ui.common.CallToActionType
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme

@Composable
fun GroupsPage() {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select a group below, or")
            Spacer(modifier = Modifier.width(16.dp))
            CallToAction(
                CallToActionType.Secondary,
                label = "Add Group",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupsPagePreview() {
    NamesInAHatTheme {
        GroupsPage()
    }
}