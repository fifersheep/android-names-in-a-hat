package uk.lobsterdoodle.namepicker.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.ui.theme.NamesInAHatTheme
import uk.lobsterdoodle.namepicker.ui.theme.RedPrimary

enum class CallToActionType {
    Primary, Secondary
}

@Composable
fun CallToAction(
    type: CallToActionType,
    label: String,
    onClick: () -> Unit
) {
    val colors = when (type) {
        CallToActionType.Primary -> ButtonDefaults.buttonColors(
            backgroundColor = RedPrimary
        )
        CallToActionType.Secondary -> ButtonDefaults.outlinedButtonColors()
    }

    Button(
        colors = colors,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        onClick = onClick,
    ) {
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
fun CallToActionPreview() {
    NamesInAHatTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CallToAction(
                CallToActionType.Primary,
                label = "Primary",
                onClick = {}
            )
            CallToAction(
                CallToActionType.Secondary,
                label = "Secondary",
                onClick = {}
            )
        }
    }
}