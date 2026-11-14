package uk.lobsterdoodle.namepicker.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import uk.lobsterdoodle.namepicker.R

/** Up, hidden in list-detail where Lists is always visible beside the detail. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun UpButton(onUp: () -> Unit, @StringRes description: Int) {
    if (LocalListDetailSceneScope.current != null) return
    IconButton(onClick = onUp) {
        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(description))
    }
}
