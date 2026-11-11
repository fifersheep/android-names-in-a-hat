package uk.lobsterdoodle.namepicker.shared.draw

import kotlin.random.Random

/** The random source for a draw, injectable so tests can script or seed it. */
fun interface NumberGenerator {
    /** Returns an index in `0 until [until]`. */
    fun nextInt(until: Int): Int
}

/** The production [NumberGenerator]. Pass a seeded [Random] for repeatable draws. */
class RandomNumberGenerator(private val random: Random = Random.Default) : NumberGenerator {
    override fun nextInt(until: Int): Int = random.nextInt(until)
}
