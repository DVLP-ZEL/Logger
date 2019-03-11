package com.skypine.elzhao.logger.app;

import android.app.Application;

import com.skypine.elzhao.logger.utils.Constant;
import com.skypine.elzhao.logger.utils.LogTrace;

public class LoggerApp extends Application {

    private LogTrace mLogTrace = new LogTrace(Constant.TAG, this.getClass().getSimpleName());

    public LoggerApp() {
        super();
        mLogTrace.i("Construct");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLogTrace.i("onCreate");
        CrashHandler.getInstance().init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mLogTrace.i("onTerminate");
    }
}
