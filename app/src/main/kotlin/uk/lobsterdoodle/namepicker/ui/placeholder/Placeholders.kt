package uk.lobsterdoodle.namepicker.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.navigation.Draw
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.components.HatIllustration
import uk.lobsterdoodle.namepicker.ui.components.UpButton
import uk.lobsterdoodle.namepicker.ui.components.sharedBoundsIfSinglePane
import uk.lobsterdoodle.namepicker.ui.lists.ListsSharedKeys

// The Draw placeholder (NAM-7). NAM-10 replaces its content, not the wiring; it keeps the title's
// shared key so the Lists card title morphs into it (spec "Motion").

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawPlaceholder(key: Draw, singlePane: Boolean, onUp: () -> Unit, onEdit: () -> Unit, onGone: (Long) -> Unit) {
    val vm = hiltViewModel<DrawViewModel, DrawViewModel.Factory>(creationCallback = { it.create(key.listId) })
    val status by vm.status.collectAsStateWithLifecycle()
    LaunchedEffect(status) { if (status == ListStatus.Gone) onGone(key.listId) }
    // Started only while the entry is on the back stack (Navigation 3 caps popped entries at CREATED).
    LifecycleStartEffect(status) {
        vm.onShown()
        onStopOrDispose {}
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (status as? ListStatus.Present)?.label.orEmpty(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.sharedBoundsIfSinglePane(ListsSharedKeys.title(key.listId), singlePane),
                    )
                },
                navigationIcon = { UpButton(onUp, R.string.draw_back) },
                actions = { TextButton(onClick = onEdit) { Text(stringResource(R.string.list_edit)) } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(key.toString(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(PaddingValues(vertical = 8.dp)))
        }
    }
}

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
