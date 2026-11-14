package uk.lobsterdoodle.namepicker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The 3.0 theme (spec "Theme — Top Hat", D20): dynamic colour on Android 12 and above, the
 * Top Hat scheme below that, light and dark following the system, expressive motion.
 */
@Composable
fun NamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = colorScheme(darkTheme),
        motionScheme = MotionScheme.expressive(),
        typography = NamesTypography,
        content = content,
    )
}

@Composable
private fun colorScheme(darkTheme: Boolean): ColorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) TopHatDark else TopHatLight
    }
