package uk.lobsterdoodle.namepicker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.lobsterdoodle.namepicker.R

/**
 * A list's or a name's text as displayed. 2.7.0 saved blank list names and names (GitHub #5, #6) and they migrate as they are;
 * 3.0 can't create them, but it must still show and announce something for them.
 */
fun displayLabel(label: String, untitled: String): String = if (label.isBlank()) untitled else label

/** [displayLabel] with the copy deck's "Untitled list". */
@Composable
fun listLabel(label: String): String = displayLabel(label, stringResource(R.string.list_untitled))

/** A name's text with the copy deck's "Unnamed" for a blank one (NAM-9). */
@Composable
fun nameLabel(text: String): String = displayLabel(text, stringResource(R.string.name_untitled))
