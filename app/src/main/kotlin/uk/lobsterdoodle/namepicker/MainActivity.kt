package uk.lobsterdoodle.namepicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import uk.lobsterdoodle.namepicker.navigation.AppNavigation
import uk.lobsterdoodle.namepicker.ui.theme.NamesTheme

/** The app's only activity: the platform splash, then everything in Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val shell: ShellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Order matters: the splash before super.onCreate, edge-to-edge after it (core-splashscreen docs).
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Up only until the launch route is known: a settings read, never the database (spec "Launch").
        splash.setKeepOnScreenCondition { shell.start.value == null }
        setContent {
            NamesTheme {
                val start by shell.start.collectAsStateWithLifecycle()
                start?.let { AppNavigation(it, shell) }
            }
        }
    }
}
