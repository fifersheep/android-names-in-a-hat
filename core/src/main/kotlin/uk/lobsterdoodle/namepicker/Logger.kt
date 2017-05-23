package uk.lobsterdoodle.namepicker

interface Logger {
    fun v(tag: String, msg: String)
    fun d(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String, ex: Throwable)
    fun wtf(tag: String, msg: String, ex: Throwable)
}
