package uk.lobsterdoodle.namepicker

import org.mockito.kotlin.any

class Testing {
    companion object Util {
        fun anyString(): String = any()
        fun anyLong(): Long = any()
        fun anyInt(): Int = any()
    }
}