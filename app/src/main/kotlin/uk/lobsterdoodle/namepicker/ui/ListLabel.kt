package uk.lobsterdoodle.namepicker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.lobsterdoodle.namepicker.R

/**
 * A list's name as displayed. 2.7.0 saved blank names (GitHub #6) and they migrate as they are;
 * 3.0 can't create them, but it must still show and announce something for them.
 */
fun displayLabel(label: String, untitled: String): String = if (label.isBlank()) untitled else label

/** [displayLabel] with the copy deck's "Untitled list". */
@Composable
fun listLabel(label: String): String = displayLabel(label, stringResource(R.string.list_untitled))
