package com.skypine.elzhao.logger.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.skypine.elzhao.logger.utils.Constant;
import com.skypine.elzhao.logger.utils.FileUtils;
import com.skypine.elzhao.logger.utils.LogTrace;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private LogTrace mLogTrace = new LogTrace(Constant.TAG, this.getClass().getSimpleName());

    private static final String PATH = "/data/crash";
    private static final String SUFFIX = ".log";
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private Context mContext;
    private static CrashHandler mInstance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public synchronized static CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    private CrashHandler() {
    }

    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        mLogTrace.i("uncaughtException", "handlerException: " + handlerException(ex));
        if (mDefaultHandler!= null) {
            //SystemClock.sleep(500);
            mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    private boolean handlerException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            saveCrashInfoFile(ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    private void saveCrashInfoFile(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        try {
             SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            sb.append("\r\n").append(date).append("\n");
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            writeFile(sb.toString());
        } catch (Exception e) {
            mLogTrace.e("saveCrashInfoFile", "an error occured while writing file...");
            sb.append("an error occured while writing file...\r\n");
            writeFile(sb.toString());
        }
    }

    private void writeFile(String content) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return;
        }
        mLogTrace.i("writeFile", "size: " + FileUtils.getDirSize(PATH));
        if (FileUtils.getDirSize(PATH) > MAX_SIZE) {
            FileUtils.clearDir(PATH);
        }
        FileUtils.makeDirs(PATH);
        String fileName = PATH + "/" + mContext.getPackageName() + SUFFIX;
        try {
            FileUtils.writeFile(fileName, content, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
