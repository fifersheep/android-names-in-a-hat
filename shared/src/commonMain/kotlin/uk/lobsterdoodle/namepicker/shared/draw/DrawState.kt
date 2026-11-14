package uk.lobsterdoodle.namepicker.shared.draw

import uk.lobsterdoodle.namepicker.shared.model.Name

/** The fewest names in the hat that make a draw available (3.0 spec D7: one name left is a valid draw). */
const val MIN_NAMES_TO_DRAW: Int = 1

/** In the hat: selected and not drawn. The pool the next draw uses (3.0 spec D2). */
val Name.inHat: Boolean get() = selected && !drawn

/**
 * The select-all checkbox (3.0 spec D18). Computed over **not-drawn** names only, because a drawn
 * chip doesn't show its selection.
 */
enum class SelectAllBox {
    /** Every not-drawn name is selected; tapping clears all. */
    ALL,

    /** Some are selected; tapping selects all. */
    SOME,

    /** None are selected; tapping selects all. */
    NONE,

    /** Every name is drawn (or there are none): nothing to toggle. */
    DISABLED,
}

/** What the draw bar offers (3.0 spec "Draw bar"). */
sealed interface DrawBarState {
    /** A count from 1 to [max] can be drawn. */
    data class Stepper(val max: Int) : DrawBarState

    /** Nothing in the hat, and no selected name is drawn: "Tap names to put them in the hat". */
    data object NothingInHat : DrawBarState

    /** Nothing in the hat because every selected name has been drawn: "Everyone's been drawn". */
    data object AllDrawn : DrawBarState
}

/** What the draw controls show for a group's names. Wording is left to the UI. */
data class DrawState(
    /** Selected and not drawn: the counter's number and the stepper's maximum. */
    val inHat: Int,
    val total: Int,
    val drawnCount: Int,
    val box: SelectAllBox,
    val bar: DrawBarState,
    val hasNames: Boolean,
) {
    val canDraw: Boolean get() = bar is DrawBarState.Stepper

    companion object {
        fun of(names: List<Name>): DrawState {
            val inHat = names.count { it.inHat }
            val notDrawn = names.filterNot { it.drawn }
            val selectedNotDrawn = notDrawn.count { it.selected }
            val box = when {
                notDrawn.isEmpty() -> SelectAllBox.DISABLED
                selectedNotDrawn == notDrawn.size -> SelectAllBox.ALL
                selectedNotDrawn == 0 -> SelectAllBox.NONE
                else -> SelectAllBox.SOME
            }
            val bar = when {
                inHat >= MIN_NAMES_TO_DRAW -> DrawBarState.Stepper(max = inHat)
                names.any { it.selected && it.drawn } -> DrawBarState.AllDrawn
                else -> DrawBarState.NothingInHat
            }
            return DrawState(
                inHat = inHat,
                total = names.size,
                drawnCount = names.count { it.drawn },
                box = box,
                bar = bar,
                hasNames = names.isNotEmpty(),
            )
        }
    }
}

/** Keeps a chosen count within 1..[inHat]; with nothing in the hat it rests at 1. */
fun clampCount(n: Int, inHat: Int): Int = n.coerceIn(1, maxOf(1, inHat))
