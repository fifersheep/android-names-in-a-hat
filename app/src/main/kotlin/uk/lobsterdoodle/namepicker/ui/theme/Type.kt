package uk.lobsterdoodle.namepicker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import uk.lobsterdoodle.namepicker.R

/** DM Serif Display (OFL, bundled; licence in assets/licenses). One upright weight only. */
internal val DmSerifDisplay = FontFamily(Font(R.font.dm_serif_display, FontWeight.Normal))

/**
 * The M3 type scale with DM Serif Display for displayLarge–Small and headlineLarge–Medium (spec
 * "Type"). Everything else, headlineSmall included (M3 dialog titles use it), stays on the system
 * default. The material3 1.5 `*Emphasized` styles are left alone too; set them here if a screen needs one.
 */
internal val NamesTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = DmSerifDisplay),
        displayMedium = displayMedium.copy(fontFamily = DmSerifDisplay),
        displaySmall = displaySmall.copy(fontFamily = DmSerifDisplay),
        headlineLarge = headlineLarge.copy(fontFamily = DmSerifDisplay),
        headlineMedium = headlineMedium.copy(fontFamily = DmSerifDisplay),
    )
}
