package uk.lobsterdoodle.namepicker.ui.placeholder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.navigation.Draw
import uk.lobsterdoodle.namepicker.navigation.EditList

// Placeholder destinations (NAM-7). Each shows its key and drives the shell's back-stack
// operations; the screen tickets replace the content, not the wiring.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsPlaceholder(onOpen: (Long) -> Unit, onNew: () -> Unit, onEdit: (Long) -> Unit) {
    val vm: ListsViewModel = hiltViewModel()
    val lists by vm.lists.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.lists_title), style = MaterialTheme.typography.headlineMedium) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNew) { Text(stringResource(R.string.lists_new)) }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { KeyLabel("Lists", Modifier.padding(horizontal = 16.dp)) }
            items(lists, key = { it.group.id }) { summary ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(summary.group.id) }.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                        Text(summary.group.label, style = MaterialTheme.typography.titleLarge)
                        Text(
                            pluralStringResource(R.plurals.lists_item_names, summary.nameCount, summary.nameCount),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    TextButton(onClick = { onEdit(summary.group.id) }) { Text(stringResource(R.string.list_edit)) }
                    TextButton(onClick = { vm.delete(summary.group.id) }) { Text(stringResource(R.string.list_delete)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawPlaceholder(key: Draw, onUp: () -> Unit, onEdit: () -> Unit, onGone: (Long) -> Unit) {
    val vm = hiltViewModel<DrawViewModel, DrawViewModel.Factory>(creationCallback = { it.create(key.listId) })
    val status by vm.status.collectAsStateWithLifecycle()
    LaunchedEffect(status) { if (status == ListStatus.Gone) onGone(key.listId) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((status as? ListStatus.Present)?.label.orEmpty(), style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = { UpButton(onUp, R.string.draw_back) },
                actions = { TextButton(onClick = onEdit) { Text(stringResource(R.string.list_edit)) } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            KeyLabel(key.toString())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListPlaceholder(
    key: EditList,
    onUp: () -> Unit,
    onCreated: (Long) -> Unit,
    onDone: () -> Unit,
    onGone: (Long) -> Unit,
) {
    val vm = hiltViewModel<EditListViewModel, EditListViewModel.Factory>(creationCallback = { it.create(key.listId) })
    val status by vm.status.collectAsStateWithLifecycle()
    LaunchedEffect(status) { if (status == ListStatus.Gone) key.listId?.let(onGone) }
    // Unsaved text. The shell's saveable-state decorator keeps it across process death.
    var name by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (key.listId == null) R.string.edit_title_new else R.string.edit_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = { UpButton(onUp, R.string.action_back) },
                actions = {
                    TextButton(onClick = onDone, enabled = key.listId != null) { Text(stringResource(R.string.edit_done)) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KeyLabel(key.toString())
            (status as? ListStatus.Present)?.let { Text(it.label, style = MaterialTheme.typography.headlineSmall) }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.edit_list_name)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (key.listId == null) {
                Button(onClick = { vm.create(name, onCreated) }, enabled = name.isNotBlank()) {
                    Text(stringResource(R.string.placeholder_create))
                }
            }
        }
    }
}

/** The detail pane when no list is open (spec "Large screens"). */
@Composable
fun PickAListPlaceholder() {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(R.string.large_pick_list),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

/** Up, hidden in list-detail where Lists is always visible beside the detail. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun UpButton(onUp: () -> Unit, description: Int) {
    if (LocalListDetailSceneScope.current != null) return
    IconButton(onClick = onUp) {
        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(description))
    }
}

@Composable
private fun KeyLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.labelMedium, modifier = modifier.padding(PaddingValues(vertical = 8.dp)))
}
