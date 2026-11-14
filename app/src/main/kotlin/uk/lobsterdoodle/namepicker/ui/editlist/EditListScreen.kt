package uk.lobsterdoodle.namepicker.ui.editlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.navigation.EditList
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.components.UpButton
import uk.lobsterdoodle.namepicker.ui.components.sharedBoundsIfSinglePane
import uk.lobsterdoodle.namepicker.ui.lists.ListsSharedKeys

/**
 * Create a list or edit one (spec "Edit list", D3). NAM-8 owns the screen and the list-name field;
 * the names section is [EditListNamesSlot], which NAM-9 fills.
 *
 * @param compactHeight phone landscape: the name field shares the top bar's row.
 * @param onCreated the list was created by this visit: the shell swaps the key (rule 1).
 * @param onDone Done: on to Draw for a new list (rule 2), back otherwise (rule 3).
 * @param onGone the list no longer exists: the shell drops this destination.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListScreen(
    key: EditList,
    singlePane: Boolean,
    compactHeight: Boolean,
    onUp: () -> Unit,
    onCreated: (Long) -> Unit,
    onDone: () -> Unit,
    onGone: (Long) -> Unit,
) {
    val vm = hiltViewModel<EditListViewModel, EditListViewModel.Factory>(creationCallback = { it.create(key.listId) })
    val status by vm.status.collectAsStateWithLifecycle()
    LaunchedEffect(status) { if (status == ListStatus.Gone) key.listId?.let(onGone) }

    val isNew = key.listId == null || key.isNew
    val nameField: @Composable (Modifier, TextStyle) -> Unit = { modifier, style ->
        ListNameField(vm, autoFocus = key.listId == null, onCreated = onCreated, textStyle = style, modifier = modifier)
    }

    Scaffold(
        // FAB → New list (spec "Motion"); the FAB carries the other end.
        modifier = if (isNew) Modifier.sharedBoundsIfSinglePane(ListsSharedKeys.NEW_LIST, singlePane) else Modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = stringResource(if (isNew) R.string.edit_title_new else R.string.edit_title)
                    if (compactHeight) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(title, style = MaterialTheme.typography.headlineMedium)
                            nameField(Modifier.weight(1f).padding(vertical = 4.dp), MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                    }
                },
                navigationIcon = { UpButton(onUp, R.string.action_back) },
                actions = {
                    TextButton(onClick = { vm.done(onDone) }, enabled = status is ListStatus.Present) {
                        Text(stringResource(R.string.edit_done))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!compactHeight) {
                nameField(
                    Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    MaterialTheme.typography.headlineSmall,
                )
            }
            EditListNamesSlot(listId = (status as? ListStatus.Present)?.let { key.listId })
        }
    }
}

/**
 * The list name: label "List name", IME Next commits it (creating the list the first time),
 * a blank commit shows "Enter a list name" and keeps focus (D15). A new list's field takes focus.
 */
@Composable
private fun ListNameField(
    vm: EditListViewModel,
    autoFocus: Boolean,
    onCreated: (Long) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    var focusedOnce by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (autoFocus && !focusedOnce) {
            focusedOnce = true
            focus.requestFocus()
        }
    }
    OutlinedTextField(
        value = vm.text,
        onValueChange = vm::onTextChange,
        label = { Text(stringResource(R.string.edit_list_name)) },
        placeholder = { Text(stringResource(R.string.edit_list_name_hint)) },
        textStyle = textStyle,
        isError = vm.error,
        supportingText = if (vm.error) {
            { Text(stringResource(R.string.edit_list_name_blank)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        // Handling Next replaces the default focus move, so focus stays here on an error. NAM-9's add
        // field takes it from here once it exists.
        keyboardActions = KeyboardActions(onNext = { vm.commitName(onCreated) }),
        modifier = modifier.focusRequester(focus),
    )
}

/**
 * The names section of Edit list: NAM-9 builds it here (the names header, the names, and the
 * docked add field, disabled with "Name the list first" while [listId] is null). Its add field
 * calls [EditListViewModel.commitName] when it takes focus (spec: "focus moving to another field").
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun ColumnScope.EditListNamesSlot(listId: Long?) = Unit
