package uk.lobsterdoodle.namepicker;

import android.util.Log;

public class AndroidLogWrapper implements Logger {
    @Override
    public void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void e(String tag, String msg, Throwable ex) {
        Log.e(tag, msg, ex);
    }

    @Override
    public void wtf(String tag, String msg, Throwable ex) {
        Log.wtf(tag, msg, ex);
    }
}
