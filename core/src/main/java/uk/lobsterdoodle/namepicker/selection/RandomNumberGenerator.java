package uk.lobsterdoodle.namepicker.selection;

import java.util.Random;

public class RandomNumberGenerator implements NumberGenerator {
    @Override
    public int randomInteger(int from) {
        return new Random().nextInt(from);
    }
}
