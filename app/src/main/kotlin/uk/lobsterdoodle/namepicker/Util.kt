package uk.lobsterdoodle.namepicker

import android.content.Context

class Util {
    companion object {
        var FILENAME = "name_list"

        fun pxForDp(context: Context, dp: Int): Int =
                Math.round(dp * context.resources.displayMetrics.density)
    }
}
