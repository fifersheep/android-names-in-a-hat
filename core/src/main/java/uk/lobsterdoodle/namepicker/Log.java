package uk.lobsterdoodle.namepicker;

public class Log {
    static private Logger logger = intoTheVoid();

    public static void install(Logger logger) {
        Log.logger = logger;
    }

    public static Logger as() {
        return logger;
    }

    private static Logger intoTheVoid() {
        return new Logger() {
            @Override
            public void v(String tag, String msg) { }

            @Override
            public void d(String tag, String msg) { }

            @Override
            public void i(String tag, String msg) { }

            @Override
            public void w(String tag, String msg) { }

            @Override
            public void e(String tag, String msg, Throwable ex) { }

            @Override
            public void wtf(String tag, String msg, Throwable ex) { }
        };
    }
}
