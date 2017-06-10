package uk.lobsterdoodle.namepicker.application.util

import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension

object As {
    fun px(context: Context, dp: Int): Int
            = applyDimension(COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
}
