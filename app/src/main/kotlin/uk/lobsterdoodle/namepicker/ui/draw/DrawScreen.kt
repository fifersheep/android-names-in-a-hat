package uk.lobsterdoodle.namepicker.ui.draw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.navigation.Draw
import uk.lobsterdoodle.namepicker.shared.model.NameSortType
import uk.lobsterdoodle.namepicker.ui.ListStatus
import uk.lobsterdoodle.namepicker.ui.components.UpButton
import uk.lobsterdoodle.namepicker.ui.components.sharedBoundsIfSinglePane
import uk.lobsterdoodle.namepicker.ui.listLabel
import uk.lobsterdoodle.namepicker.ui.lists.ListsSharedKeys
import uk.lobsterdoodle.namepicker.ui.theme.DmSerifDisplay

/**
 * Draw names from one list (spec "Draw"; NAM-10). Navigation stays with the caller.
 *
 * @param singlePane the title morphs from the Lists card (spec "Motion"), single pane only.
 * @param compactHeight phone landscape: a single-row top bar and the draw bar as a trailing panel.
 * @param onEdit the overflow's Edit list, and the empty state's Add names (both push `EditList(id)`).
 * @param onGone the list no longer exists: the shell drops this destination.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawScreen(
    key: Draw,
    singlePane: Boolean,
    compactHeight: Boolean,
    onUp: () -> Unit,
    onEdit: () -> Unit,
    onGone: (Long) -> Unit,
    vm: DrawViewModel = hiltViewModel<DrawViewModel, DrawViewModel.Factory>(creationCallback = { it.create(key.listId) }),
) {
    val status by vm.status.collectAsStateWithLifecycle()
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(status) { if (status == ListStatus.Gone) onGone(key.listId) }
    // Started only while the entry is on the back stack (Navigation 3 caps popped entries at CREATED).
    LifecycleStartEffect(status) {
        vm.onShown()
        onStopOrDispose {}
    }

    val label = (status as? ListStatus.Present)?.let { listLabel(it.label) }.orEmpty()
    val titleModifier = Modifier.sharedBoundsIfSinglePane(ListsSharedKeys.title(key.listId), singlePane)
    val hasNames = state.loaded && state.draw.hasNames
    val actions: @Composable () -> Unit = {
        if (hasNames) SortPill(state.sort, vm::cycleSort)
        DrawOverflow(onEdit)
    }
    val scrollBehavior = if (compactHeight) null else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier,
        topBar = {
            if (compactHeight) {
                TopAppBar(
                    title = {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = DmSerifDisplay),
                            maxLines = 1,
                            modifier = titleModifier,
                        )
                    },
                    navigationIcon = { UpButton(onUp, R.string.draw_back) },
                    actions = { actions() },
                )
            } else {
                MediumFlexibleTopAppBar(
                    title = { Text(label, style = MaterialTheme.typography.headlineLarge, modifier = titleModifier) },
                    navigationIcon = { UpButton(onUp, R.string.draw_back) },
                    actions = { actions() },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            if (hasNames && !compactHeight) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    DrawBar(
                        state.draw, state.count, vertical = false,
                        onFewer = vm::decrement, onMore = vm::increment, onDraw = vm::draw,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    )
                }
            }
        },
    ) { padding ->
        when {
            !state.loaded -> Unit
            !state.draw.hasNames -> DrawEmptyState(onAddNames = onEdit, modifier = Modifier.fillMaxSize().padding(padding))
            compactHeight -> Row(Modifier.fillMaxSize().padding(padding)) {
                NamesArea(state, vm, Modifier.weight(1f))
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxHeight()) {
                    DrawBar(
                        state.draw, state.count, vertical = true,
                        onFewer = vm::decrement, onMore = vm::increment, onDraw = vm::draw,
                    )
                }
            }
            else -> NamesArea(state, vm, Modifier.fillMaxSize().padding(padding))
        }
    }

}

/** The sticky select-all header over the scrolling chips. */
@Composable
private fun NamesArea(state: DrawUiState, vm: DrawViewModel, modifier: Modifier) {
    Column(modifier) {
        SelectAllHeader(state.draw, onToggle = vm::toggleAll)
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            NameChips(state.chips, onToggle = vm::toggle)
        }
    }
}

/** The sort button: a tonal pill with the current order, cycling on each tap (D17). */
@Composable
private fun SortPill(sort: NameSortType, onClick: () -> Unit) {
    val text = stringResource(
        when (sort) {
            NameSortType.NONE -> R.string.draw_sort_unsorted
            NameSortType.ASC -> R.string.draw_sort_az
            NameSortType.DESC -> R.string.draw_sort_za
        },
    )
    val description = stringResource(R.string.draw_sort_a11y, text)
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        modifier = Modifier.height(40.dp).semantics { contentDescription = description },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_sort), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** The overflow: Edit list. */
@Composable
private fun DrawOverflow(onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.list_edit)) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
        }
    }
}
