package uk.lobsterdoodle.namepicker.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import uk.lobsterdoodle.namepicker.navigation.LocalSharedTransitionScope

/**
 * Shared bounds between two destinations (spec "Motion": FAB → New list, card title → Draw title),
 * in single pane only. In list-detail both ends are on screen at once, so there is nothing to morph.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsIfSinglePane(key: Any, singlePane: Boolean): Modifier {
    if (!singlePane) return this
    val shared = LocalSharedTransitionScope.current ?: return this
    val content = LocalNavAnimatedContentScope.current
    return with(shared) { this@sharedBoundsIfSinglePane.sharedBounds(rememberSharedContentState(key), content) }
}
