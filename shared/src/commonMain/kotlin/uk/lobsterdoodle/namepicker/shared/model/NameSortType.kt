package uk.lobsterdoodle.namepicker.shared.model

/**
 * How a group's names are ordered on the draw screen.
 *
 * The constant names are a stored format: 2.7.0 writes them to preferences as strings,
 * so they must never be renamed.
 */
enum class NameSortType {
    NONE, ASC, DESC;

    /** The order selected by the next press of the sort action: NONE → ASC → DESC → NONE (3.0 spec D17). */
    fun next(): NameSortType = when (this) {
        NONE -> ASC
        ASC -> DESC
        DESC -> NONE
    }

    companion object {
        /** Reads a stored value leniently: anything unrecognised, or nothing, is [NONE]. */
        fun fromStored(value: String?): NameSortType = entries.firstOrNull { it.name == value } ?: NONE
    }
}
