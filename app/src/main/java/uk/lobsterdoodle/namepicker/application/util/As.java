package uk.lobsterdoodle.namepicker.application.util;

import android.content.Context;
import android.util.DisplayMetrics;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public class As {
    public static int px(Context context, int dp) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) applyDimension(COMPLEX_UNIT_DIP, dp, metrics);
    }
}
