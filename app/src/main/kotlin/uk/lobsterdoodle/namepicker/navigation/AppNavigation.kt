package uk.lobsterdoodle.namepicker.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import uk.lobsterdoodle.namepicker.ShellViewModel
import uk.lobsterdoodle.namepicker.ui.placeholder.DrawPlaceholder
import uk.lobsterdoodle.namepicker.ui.placeholder.EditListPlaceholder
import uk.lobsterdoodle.namepicker.ui.placeholder.ListsPlaceholder
import uk.lobsterdoodle.namepicker.ui.placeholder.PickAListPlaceholder

/**
 * The scope for shared-element transitions between destinations (spec "Motion": Lists → Draw,
 * FAB → New list). The shell provides it; the screen tickets add the morphs. Use it with
 * `LocalNavAnimatedContentScope` from navigation3-ui.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * The navigation host: the back stack (saved across process death), list-detail on large windows,
 * and per-entry saved state and ViewModels.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(start: List<AppKey>, shell: ShellViewModel) {
    // The initial keys apply only to a fresh start; a restored stack wins.
    val backStack = rememberNavBackStack(*start.toTypedArray())

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val sizeClass = adaptiveInfo.windowSizeClass
    val listDetail = isListDetail(sizeClass)
    val defaultDirective = calculatePaneScaffoldDirective(adaptiveInfo)
    val directive = remember(defaultDirective, listDetail) {
        if (listDetail) {
            defaultDirective.copy(maxHorizontalPartitions = 2)
        } else {
            defaultDirective.copy(
                maxHorizontalPartitions = 1,
                horizontalPartitionSpacerSize = 0.dp,
                maxVerticalPartitions = 1,
                verticalPartitionSpacerSize = 0.dp,
            )
        }
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        // One entry per back, so back from Edit list in the detail pane returns to Draw.
        backNavigationBehavior = BackNavigationBehavior.PopLatest,
        directive = directive,
    )

    // Back gesture and Up do the same thing: pop one entry, and on a phone a back-out from Draw
    // clears the active list (spec "Launch").
    val back: () -> Unit = { backStack.back(singlePane = !listDetail)?.let { shell.onLeftList() } }
    val listGone: (Long) -> Unit = { id ->
        backStack.listGone(id)
        shell.onListGone(id)
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = back,
                entryDecorators = listOf(
                    // Saved state first: rememberSaveable (in-progress text included) per entry,
                    // across process death. Then a ViewModelStore per entry.
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sceneStrategies = listOf(listDetailStrategy),
                sharedTransitionScope = this,
                entryProvider = entryProvider {
                    entry<Lists>(
                        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { PickAListPlaceholder() }) +
                            ListDetailSceneStrategy.preferredPaneSize(width = listPaneWidth(sizeClass)),
                    ) {
                        ListsPlaceholder(
                            onOpen = { backStack.openList(it) },
                            onNew = { backStack.newList() },
                            onEdit = { backStack.editList(it, fromDraw = false) },
                        )
                    }
                    entry<Draw>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                        DrawPlaceholder(
                            key = key,
                            onUp = back,
                            onEdit = { backStack.editList(key.listId, fromDraw = true) },
                            onGone = listGone,
                        )
                    }
                    entry<EditList>(
                        // One content key per visit: creating the list swaps the key but keeps the
                        // entry; another visit never inherits this one's state.
                        clazzContentKey = { "EditList:${it.session}" },
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) { key ->
                        EditListPlaceholder(
                            key = key,
                            onUp = back,
                            onCreated = { backStack.listCreated(it, key.session) },
                            onDone = { backStack.doneEditing() },
                            onGone = listGone,
                        )
                    }
                },
            )
        }
    }
}
