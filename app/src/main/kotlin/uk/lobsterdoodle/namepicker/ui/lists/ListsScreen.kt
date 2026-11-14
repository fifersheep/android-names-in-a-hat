package uk.lobsterdoodle.namepicker.ui.lists

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.ui.listLabel
import uk.lobsterdoodle.namepicker.ui.components.HatIllustration
import uk.lobsterdoodle.namepicker.ui.components.sharedBoundsIfSinglePane

/** Shared-element keys between Lists and the screens it opens (spec "Motion"). */
object ListsSharedKeys {
    const val NEW_LIST = "new-list"

    fun title(listId: Long) = "list-title-$listId"
}

/**
 * Every list (spec "Lists"). Navigation stays with the caller:
 *
 * @param openId the list shown in the detail pane in list-detail, or null.
 * @param singlePane Lists is the whole window, so leaving it ends any undo window.
 * @param compactHeight phone landscape: small top bar, cards at most 640 dp wide.
 * @param onHideOpen the open list was just deleted: close the detail and clear the active list.
 * @param onReopen Undo of a list that was open: show it in the detail again.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    openId: Long?,
    singlePane: Boolean,
    compactHeight: Boolean,
    onOpen: (Long) -> Unit,
    onNew: () -> Unit,
    onEdit: (Long) -> Unit,
    onHideOpen: (Long) -> Unit,
    onReopen: (Long) -> Unit,
    vm: ListsViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var aboutOpen by rememberSaveable { mutableStateOf(false) }

    DelayedDeleteSnackbar(state.pending, snackbar, vm, openId, onReopen)

    // Leaving the app ends the undo window: on Android 12+ back from here only backgrounds the
    // task, so the ViewModel may never be cleared. A rotation is not leaving.
    val activity = LocalActivity.current
    LifecycleStartEffect(vm) {
        onStopOrDispose { if (activity?.isChangingConfigurations != true) vm.commitPending() }
    }

    // In single pane, opening another screen hides Lists and its snackbar, so commit first. In
    // list-detail they stay on screen, and so does Undo.
    fun leaving(go: () -> Unit) {
        if (singlePane) vm.commitPending()
        go()
    }

    val scrollBehavior =
        if (compactHeight || !singlePane) null else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier,
        topBar = {
            val title: @Composable () -> Unit = { Text(stringResource(R.string.lists_title)) }
            val actions: @Composable () -> Unit = { ListsOverflow(onAbout = { aboutOpen = true }) }
            if (compactHeight) {
                TopAppBar(
                    title = { Text(stringResource(R.string.lists_title), style = MaterialTheme.typography.headlineMedium) },
                    actions = { actions() },
                )
            } else {
                LargeFlexibleTopAppBar(title = title, actions = { actions() }, scrollBehavior = scrollBehavior)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { leaving(onNew) },
                // The FAB's accessible label comes from its icon: material3 clears the text's semantics.
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.lists_new)) },
                text = { Text(stringResource(R.string.lists_new)) },
                modifier = Modifier.sharedBoundsIfSinglePane(ListsSharedKeys.NEW_LIST, singlePane),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            !state.loaded -> Unit
            state.lists.isEmpty() -> EmptyLists(Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Room for the FAB below the last card.
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(state.lists, key = { _, card -> card.id }) { index, card ->
                    ListCardItem(
                        card = card,
                        index = index,
                        count = state.lists.size,
                        selected = !singlePane && card.id == openId,
                        singlePane = singlePane,
                        onOpen = { leaving { onOpen(card.id) } },
                        onEdit = { leaving { onEdit(card.id) } },
                        onDelete = {
                            val wasOpen = !singlePane && card.id == openId
                            vm.delete(card.id, card.label, wasOpen)
                            if (wasOpen) onHideOpen(card.id)
                        },
                        modifier = Modifier
                            .animateItem()
                            .then(if (compactHeight) Modifier.widthIn(max = 640.dp) else Modifier)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (aboutOpen) AboutSheet(onDismiss = { aboutOpen = false })
}

/**
 * The "<list> deleted · Undo" snackbar for the pending delete. Undo un-hides the list, and reopens
 * it if it was open and the detail pane is still empty (the user may have opened another list
 * since). The snackbar timing out commits the delete. Each result names its own list, so a late
 * result never touches a newer delete. Re-shown after rotation.
 */
@Composable
private fun DelayedDeleteSnackbar(
    pending: PendingDelete?,
    snackbar: SnackbarHostState,
    vm: ListsViewModel,
    openId: Long?,
    onReopen: (Long) -> Unit,
) {
    val message = pending?.let { stringResource(R.string.list_deleted, listLabel(it.label)) }
    val undo = stringResource(R.string.action_undo)
    val reopen by rememberUpdatedState(onReopen)
    val detailOpen by rememberUpdatedState(openId)
    LaunchedEffect(pending?.id) {
        val id = pending?.id ?: return@LaunchedEffect
        // Explicit: with an action, the default duration is Indefinite, which would never commit.
        when (snackbar.showSnackbar(message.orEmpty(), actionLabel = undo, duration = SnackbarDuration.Long)) {
            SnackbarResult.ActionPerformed -> vm.undo(id)?.let { if (it.reopen && detailOpen == null) reopen(it.id) }
            SnackbarResult.Dismissed -> vm.commit(id)
        }
    }
}

@Composable
private fun ListCardItem(
    card: ListCard,
    index: Int,
    count: Int,
    selected: Boolean,
    singlePane: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The canvas fills cards with surfaceContainer, and the open list with secondaryContainer
    // (ListsScreen.dc.html); M3's segmented default is close to the surface and barely shows.
    val colors = ListItemDefaults.segmentedColors(
        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
    )
    SegmentedListItem(
        onClick = onOpen,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = colors,
        modifier = modifier,
        supportingContent = { Text(countLine(card)) },
        trailingContent = { CardOverflow(listLabel(card.label), onEdit, onDelete) },
    ) {
        Text(
            listLabel(card.label),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.sharedBoundsIfSinglePane(ListsSharedKeys.title(card.id), singlePane),
        )
    }
}

/** "24 names", or "24 names · 4 drawn" once any are drawn (plurals for both). */
@Composable
private fun countLine(card: ListCard): String {
    val names = pluralStringResource(R.plurals.lists_item_names, card.nameCount, card.nameCount)
    if (card.drawnCount == 0) return names
    return names + " " + pluralStringResource(R.plurals.lists_item_drawn, card.drawnCount, card.drawnCount)
}

@Composable
private fun CardOverflow(label: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.list_options, label))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.list_edit)) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.list_delete)) },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun ListsOverflow(onAbout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about)) },
                onClick = {
                    expanded = false
                    onAbout()
                },
            )
        }
    }
}

/** No lists yet (spec "States"): the hat, a title, a line of help. The FAB stays. */
@Composable
private fun EmptyLists(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(start = 40.dp, end = 40.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HatIllustration()
        Text(
            stringResource(R.string.lists_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.lists_empty_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
