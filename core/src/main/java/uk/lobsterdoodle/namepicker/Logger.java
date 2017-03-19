package uk.lobsterdoodle.namepicker;

public interface Logger {
    void v(String tag, String msg);

    void d(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg, Throwable ex);

    void wtf(String tag, String msg, Throwable ex);
}
