package uk.lobsterdoodle.namepicker.navigation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND

/**
 * List-detail needs width >= 840 dp **and** height >= 480 dp (spec "Large screens", D16). The
 * default pane directive ignores height, which would split a phone in landscape.
 */
fun isListDetail(sizeClass: WindowSizeClass): Boolean =
    sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) &&
        sizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)

/** The Lists pane: 360 dp, or 400 dp from 1600 dp wide. */
fun listPaneWidth(sizeClass: WindowSizeClass): Dp =
    if (sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND)) 400.dp else 360.dp
