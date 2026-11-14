package uk.lobsterdoodle.namepicker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import uk.lobsterdoodle.namepicker.R

/** DM Serif Display (OFL, bundled; licence in assets/licenses). One upright weight only. */
internal val DmSerifDisplay = FontFamily(Font(R.font.dm_serif_display, FontWeight.Normal))

/**
 * The M3 type scale with DM Serif Display for display and headline styles (spec "Type").
 * Everything else stays on the system default.
 */
internal val NamesTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = DmSerifDisplay),
        displayMedium = displayMedium.copy(fontFamily = DmSerifDisplay),
        displaySmall = displaySmall.copy(fontFamily = DmSerifDisplay),
        headlineLarge = headlineLarge.copy(fontFamily = DmSerifDisplay),
        headlineMedium = headlineMedium.copy(fontFamily = DmSerifDisplay),
        headlineSmall = headlineSmall.copy(fontFamily = DmSerifDisplay),
    )
}
