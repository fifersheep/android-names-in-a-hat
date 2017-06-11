package uk.lobsterdoodle.namepicker.selection

import java.util.Random

class RandomNumberGenerator : NumberGenerator {
    override fun randomInteger(from: Int): Int {
        return Random().nextInt(from)
    }
}
