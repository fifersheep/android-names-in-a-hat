package uk.lobsterdoodle.namepicker

import com.nhaarman.mockito_kotlin.any

class Testing {
    companion object Util {
        fun anyString(): String = any()
        fun anyLong(): Long = any()
        fun anyInt(): Int = any()
    }
}