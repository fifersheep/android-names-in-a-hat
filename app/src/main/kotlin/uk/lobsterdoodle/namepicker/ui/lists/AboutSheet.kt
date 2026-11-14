package uk.lobsterdoodle.namepicker.ui.lists

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.R

/** The published privacy policy (spec "About"). */
private const val PRIVACY_POLICY_URL = "https://fifersheep.github.io/android-names-in-a-hat/privacy-policy.html"

/** About, from the Lists overflow (spec "About", D19). There is no settings screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) { versionName(context) }
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Decorative: the app name follows.
            Image(painterResource(R.drawable.ic_app_badge), contentDescription = null, modifier = Modifier.size(72.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            if (version != null) {
                Text(
                    stringResource(R.string.about_version, version),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.about_made_by), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.about_previously),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
                Text(stringResource(R.string.about_privacy))
                Spacer(Modifier.size(8.dp))
                Icon(painterResource(R.drawable.ic_open_in_new), contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun versionName(context: Context): String? = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
} catch (e: PackageManager.NameNotFoundException) {
    null
}
