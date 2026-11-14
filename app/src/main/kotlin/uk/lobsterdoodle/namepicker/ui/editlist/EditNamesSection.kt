package uk.lobsterdoodle.namepicker.ui.editlist

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.ui.nameLabel

/** Content is capped and centred on wide panes, like the list-name field (spec "Large screens"). */
private val MaxContentWidth = 840.dp

/** Horizontal safe-drawing insets: the Column above only takes the top bar's padding. */
private val SideInsets = WindowInsetsSides.Horizontal

/**
 * The names section of Edit list (spec "Edit list"; NAM-9): the "N names" header, the names in the
 * order added, and the add field docked at the bottom above the keyboard. Until the list exists
 * ([listId] null) only the add field shows, disabled, with "Name the list first".
 *
 * @param listId the list, once it exists **and** is present (NAM-8 review A3: from the ViewModel).
 * @param focusAdd Next on the list name asked for focus to move here; [onFocusAddHandled] clears it.
 * @param onFieldFocused one of this section's fields took focus: the list name commits (spec:
 *   "focus moving to another field").
 */
@Composable
fun ColumnScope.EditListNamesSlot(
    listId: Long?,
    focusAdd: Boolean,
    onFocusAddHandled: () -> Unit,
    onFieldFocused: () -> Unit,
) {
    val vm = listId?.let { id ->
        hiltViewModel<EditNamesViewModel, EditNamesViewModel.Factory>(
            key = "names:$id",
            creationCallback = { it.create(id) },
        )
    }
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: EditNamesUiState()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val addFocus = remember { FocusRequester() }
    var scrollToNew by rememberSaveable { mutableStateOf(false) }

    if (vm != null) {
        DelayedDeleteSnackbar(state.pending, snackbar, vm)
        // Leaving the app ends the undo window; a rotation is not leaving. Disposal (the screen
        // closing) commits too; the ViewModel skips it if the list itself is being deleted.
        val activity = LocalActivity.current
        LifecycleStartEffect(vm) {
            onStopOrDispose { if (activity?.isChangingConfigurations != true) vm.commitPending() }
        }
    }

    // Next on the list name moves focus here, once the field is enabled (the list may still be
    // being created when Next is pressed).
    LaunchedEffect(vm != null, focusAdd) {
        if (vm != null && focusAdd) {
            addFocus.requestFocus()
            onFocusAddHandled()
        }
    }

    // The list scrolls to show a name just added, once it arrives from the database.
    LaunchedEffect(state.names.size) {
        if (scrollToNew && state.names.isNotEmpty()) {
            listState.animateScrollToItem(state.names.lastIndex)
            scrollToNew = false
        }
    }

    if (vm != null && state.loaded) {
        Text(
            pluralStringResource(R.plurals.edit_names_count, state.names.size, state.names.size),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(SideInsets))
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
        )
    }

    Box(Modifier.weight(1f).fillMaxWidth()) {
        if (vm != null) {
            LazyColumn(
                state = listState,
                contentPadding = WindowInsets.safeDrawing.only(SideInsets).asPaddingValues(),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.names, key = { it.id }) { row ->
                    val rowModifier = Modifier.animateItem().widthIn(max = MaxContentWidth).fillMaxWidth()
                    if (vm.renamingId == row.id) {
                        RenameRow(vm, onFieldFocused, rowModifier)
                    } else {
                        NameRowItem(
                            row = row,
                            onRename = { vm.startRename(row.id, row.text) },
                            onDelete = { vm.delete(row.id, row.text) },
                            modifier = rowModifier,
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    AddNameBar(
        text = vm?.addText.orEmpty(),
        enabled = vm != null,
        error = vm?.addError == true,
        onTextChange = { vm?.onAddTextChange(it) },
        onAdd = { if (vm?.add() == true) scrollToNew = true },
        onFocused = onFieldFocused,
        focus = addFocus,
    )
}

/** One name: tap it to rename inline (D13); the trailing button deletes it. */
@Composable
private fun NameRowItem(row: NameRow, onRename: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val label = nameLabel(row.text)
    Column(modifier) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp).padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                // A blank 2.7.0 name reads "Unnamed", set apart from real names.
                color = if (row.text.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickable(
                        onClickLabel = stringResource(R.string.edit_rename_name, label),
                        role = Role.Button,
                        onClick = onRename,
                    )
                    .wrapContentHeight(Alignment.CenterVertically),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.edit_delete_name, label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** A name being renamed: a field with ✓ and ✕. IME Done or ✓ saves; blank shows "Enter a name". */
@Composable
private fun RenameRow(vm: EditNamesViewModel, onFocused: () -> Unit, modifier: Modifier = Modifier) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val blank = stringResource(R.string.edit_name_blank)
    Column(modifier) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp).padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = vm.renameText,
                onValueChange = vm::onRenameTextChange,
                singleLine = true,
                isError = vm.renameError,
                supportingText = if (vm.renameError) {
                    { Text(blank) }
                } else {
                    null
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                // Handling Done keeps the keyboard up on an error; a save closes the row.
                keyboardActions = KeyboardActions(onDone = { vm.saveRename() }),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .focusRequester(focus)
                    .onFocusChanged { if (it.isFocused) onFocused() },
            )
            IconButton(onClick = { vm.saveRename() }) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.edit_rename_save),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = vm::cancelRename) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.edit_rename_cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * The add field, docked at the bottom: its `surfaceContainer` runs edge to edge, and safe-drawing
 * insets (which include the keyboard) keep the field above the navigation bar or the IME. IME Send
 * adds and keeps focus, so several names can be typed in a row.
 */
@Composable
private fun AddNameBar(
    text: String,
    enabled: Boolean,
    error: Boolean,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    onFocused: () -> Unit,
    focus: FocusRequester,
) {
    val support = when {
        !enabled -> stringResource(R.string.edit_name_first)
        error -> stringResource(R.string.edit_name_blank)
        else -> null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.widthIn(max = MaxContentWidth).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = enabled,
                label = { Text(stringResource(R.string.edit_add_name)) },
                placeholder = { Text(stringResource(R.string.edit_add_name_hint)) },
                isError = error,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Send),
                // Handling Send replaces the default, so focus and the keyboard stay.
                keyboardActions = KeyboardActions(onSend = { onAdd() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focus)
                    .onFocusChanged { if (it.isFocused) onFocused() }
                    .semantics { if (error && support != null) error(support) },
            )
            FilledIconButton(onClick = onAdd, enabled = enabled, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.edit_add_name_action))
            }
        }
        if (support != null) {
            Text(
                support,
                style = MaterialTheme.typography.bodySmall,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = MaxContentWidth).fillMaxWidth().padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * The "<name> deleted · Undo" snackbar for the pending delete. Undo un-hides the name; the snackbar
 * ending commits it. Each result names its own delete, so a late one never touches a newer delete.
 * Re-shown after rotation.
 */
@Composable
private fun DelayedDeleteSnackbar(pending: PendingNameDelete?, snackbar: SnackbarHostState, vm: EditNamesViewModel) {
    val message = pending?.let { stringResource(R.string.name_deleted, nameLabel(it.text)) }
    val undo = stringResource(R.string.action_undo)
    LaunchedEffect(pending?.id) {
        val id = pending?.id ?: return@LaunchedEffect
        // Explicit: with an action, the default duration is Indefinite, which would never commit.
        when (snackbar.showSnackbar(message.orEmpty(), actionLabel = undo, duration = SnackbarDuration.Long)) {
            SnackbarResult.ActionPerformed -> vm.undo(id)
            SnackbarResult.Dismissed -> vm.commit(id)
        }
    }
}
