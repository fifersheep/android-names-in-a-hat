package uk.lobsterdoodle.namepicker.shared.selection

import uk.lobsterdoodle.namepicker.shared.model.Name
import uk.lobsterdoodle.namepicker.shared.model.NameSortType

/**
 * Orders [names] for display. ASC and DESC compare ignoring case; NONE keeps the order given
 * (the repository's insertion order). The sort is stable, so names that compare equal keep
 * their relative order.
 */
fun sortNames(names: List<Name>, type: NameSortType): List<Name> = when (type) {
    NameSortType.NONE -> names
    NameSortType.ASC -> names.sortedWith { a, b -> a.text.compareTo(b.text, ignoreCase = true) }
    NameSortType.DESC -> names.sortedWith { a, b -> b.text.compareTo(a.text, ignoreCase = true) }
}
