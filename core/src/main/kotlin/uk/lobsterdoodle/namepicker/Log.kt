package uk.lobsterdoodle.namepicker

object Log {
    private var logger = noOp()

    fun install(logger: Logger) {
        Log.logger = logger
    }

    private fun noOp(): Logger {
        return object : Logger {
            override fun v(tag: String, msg: String) {}
            override fun d(tag: String, msg: String) {}
            override fun i(tag: String, msg: String) {}
            override fun w(tag: String, msg: String) {}
            override fun e(tag: String, msg: String, ex: Throwable) {}
            override fun wtf(tag: String, msg: String, ex: Throwable) {}
        }
    }
}
