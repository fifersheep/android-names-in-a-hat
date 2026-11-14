package uk.lobsterdoodle.namepicker.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.ui.components.HatIllustration

// The detail pane's placeholder when no list is open (NAM-7, NAM-8).

/** The detail pane when no list is open (spec "Large screens"). */
@Composable
fun PickAListPlaceholder() {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HatIllustration()
        Text(
            stringResource(R.string.large_pick_list),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}
