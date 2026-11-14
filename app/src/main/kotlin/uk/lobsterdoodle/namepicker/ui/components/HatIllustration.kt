package uk.lobsterdoodle.namepicker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The top hat with sparkles, for empty states (spec "States"; art from
 * `docs/design/3.0-mockups/project/ListsScreen.dc.html:43-50`). Built in Compose rather than as a
 * drawable so its two colours follow the Compose colour scheme, dynamic colour included: a resource
 * vector takes one tint, and its theme attributes resolve against the platform theme. Decorative:
 * the text beside it says what it means.
 */
@Composable
fun HatIllustration(modifier: Modifier = Modifier, size: Dp = 140.dp) {
    val hat = MaterialTheme.colorScheme.primary
    val sparkle = MaterialTheme.colorScheme.tertiary
    val image = remember(hat, sparkle) { hatVector(hat, sparkle) }
    Image(image, contentDescription = null, modifier = modifier.size(size))
}

private fun hatVector(hat: Color, sparkle: Color): ImageVector =
    ImageVector.Builder(
        name = "Hat",
        defaultWidth = 108.dp,
        defaultHeight = 108.dp,
        viewportWidth = 108f,
        viewportHeight = 108f,
    ).apply {
        fun path(data: String, color: Color) = addPath(addPathNodes(data), fill = SolidColor(color))
        path("M30,24 L31.5,28.5 L36,30 L31.5,31.5 L30,36 L28.5,31.5 L24,30 L28.5,28.5 Z", sparkle)
        path("M78,19 L79.2,22.8 L83,24 L79.2,25.2 L78,29 L76.8,25.2 L73,24 L76.8,22.8 Z", sparkle)
        path("M84,38 L85,41 L88,42 L85,43 L84,46 L83,43 L80,42 L83,41 Z", sparkle)
        path("M38,32 L70,32 L68,66 L40,66 Z", hat)
        path("M39.5,56 H68.5 V62 H39.5 Z", sparkle)
        path("M30,64 H78 A4,4 0 0 1 82,68 A4,4 0 0 1 78,72 H30 A4,4 0 0 1 26,68 A4,4 0 0 1 30,64 Z", hat)
    }.build()
