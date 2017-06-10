package uk.lobsterdoodle.namepicker

import android.util.Log

class AndroidLogWrapper : Logger {
    override fun v(tag: String, msg: String) { Log.v(tag, msg) }

    override fun d(tag: String, msg: String) { Log.d(tag, msg) }

    override fun i(tag: String, msg: String) { Log.i(tag, msg) }

    override fun w(tag: String, msg: String) { Log.w(tag, msg) }

    override fun e(tag: String, msg: String, ex: Throwable) { Log.e(tag, msg, ex) }

    override fun wtf(tag: String, msg: String, ex: Throwable) { Log.wtf(tag, msg, ex) }
}
