package uk.lobsterdoodle.namepicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import uk.lobsterdoodle.namepicker.navigation.AppKey
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
                start?.let { RoutePlaceholder(it) }
            }
        }
    }
}

/** Replaced by the navigation host in the next task. */
@Composable
private fun RoutePlaceholder(start: List<AppKey>) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = start.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}
