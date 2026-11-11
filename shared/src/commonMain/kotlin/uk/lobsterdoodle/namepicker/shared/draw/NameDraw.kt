package uk.lobsterdoodle.namepicker.shared.draw

import uk.lobsterdoodle.namepicker.shared.model.Name

/** Draws names at random, without replacement. */
class NameDraw(private val generator: NumberGenerator) {

    /**
     * Draws up to [count] items from [pool], removing each pick before the next.
     *
     * Asking for more than [pool] holds returns every item once; a count of zero or less
     * returns nothing. Never throws.
     */
    fun <T> draw(pool: List<T>, count: Int): List<T> {
        val remaining = pool.toMutableList()
        return List(count.coerceIn(0, pool.size)) {
            remaining.removeAt(generator.nextInt(remaining.size))
        }
    }

    /** Draws up to [count] of the selected [names]; unselected names never take part. */
    fun drawFrom(names: List<Name>, count: Int): List<Name> = draw(names.filter { it.selected }, count)
}
