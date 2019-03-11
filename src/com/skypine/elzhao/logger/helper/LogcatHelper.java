package com.skypine.elzhao.logger.helper;

import com.skypine.elzhao.logger.utils.Constant;
import com.skypine.elzhao.logger.utils.FileUtils;
import com.skypine.elzhao.logger.utils.LogTrace;
import com.skypine.elzhao.logger.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogcatHelper {

    private LogTrace mLogTrace = new LogTrace(Constant.TAG, this.getClass().getSimpleName());

    private static final int DEFAULT_THREAD_NUM = 8;

    private static LogcatHelper mInstance;
    private ExecutorService mExecutorService;
    private List<Task> mTasks = new ArrayList<>();

    public synchronized static LogcatHelper getInstance() {
        if (mInstance == null) {
            mInstance = new LogcatHelper();
        }
        return mInstance;
    }

    private LogcatHelper() {
        mLogTrace.i("construct");
        mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);
    }

    public void start(File dir) {
        mTasks.clear();
        mTasks.add(new LogcatTask(dir, LogcatTask.TYPE_LOGCAT, "Logcat"));
        mTasks.add(new LogcatTask(dir, LogcatTask.TYPE_MAIN, "Main"));
        mTasks.add(new LogcatTask(dir, LogcatTask.TYPE_SYSTEM, "System"));
        mTasks.add(new LogcatTask(dir, LogcatTask.TYPE_RADIO, "Radio"));
        mTasks.add(new LogcatTask(dir, LogcatTask.TYPE_EVENTS, "Events"));
        mTasks.add(new AnrSaveTask(dir, "ANR"));
        mTasks.add(new CrashSaveTask(dir, "Crash"));
        for (Task t : mTasks) {
            mExecutorService.execute(t);
        }
    }

    public void stop() {
        mLogTrace.i("stop");
        for (Task t : mTasks) {
            t.stop();
        }
    }

    public boolean isRunning() {
        for (Task t : mTasks) {
            if (t.isRunning()) {
                mLogTrace.i("isRunning", "t: " + t.getTaskName());
                return true;
            }
        }
        return false;
    }

    private class LogcatTask implements Task {

        //private static final int DEFAULT_SIZE = 1024;

        private static final int TYPE_LOGCAT = 1;
        private static final int TYPE_MAIN = 2;
        private static final int TYPE_SYSTEM = 3;
        private static final int TYPE_RADIO = 4;
        private static final int TYPE_EVENTS = 5;

        private static final String NAME_LOGCAT = "log_logcat.log";
        private static final String NAME_MAIN = "log_main.log";
        private static final String NAME_SYSTEM = "log_system.log";
        private static final String NAME_RADIO = "log_radio.log";
        private static final String NAME_EVENTS = "log_events.log";

        private File mOutDir;
        private Process mProcess;

        private String mCmd;
        private boolean mRunning;
        private String mTaskName;

        LogcatTask(File dir, int type, String taskName) {
            mOutDir = dir;
            mTaskName = taskName;
            switch (type) {
                case TYPE_LOGCAT:
                    mCmd = "logcat -v time";
                    mOutDir = new File(dir, NAME_LOGCAT);
                    break;
                case TYPE_MAIN:
                    mCmd = "logcat -b main -v time";
                    mOutDir = new File(dir, NAME_MAIN);
                    break;
                case TYPE_SYSTEM:
                    mCmd = "logcat -b system -v time";
                    mOutDir = new File(dir, NAME_SYSTEM);
                    break;
                case TYPE_RADIO:
                    mCmd = "logcat -b radio -v time";
                    mOutDir = new File(dir, NAME_RADIO);
                    break;
                case TYPE_EVENTS:
                    mCmd = "logcat -b events -v time";
                    mOutDir = new File(dir, NAME_EVENTS);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void run() {
            mRunning = true;
            InputStream is = null;
            OutputStream os = null;
            try {
                mLogTrace.i("run", "start: " + mTaskName);
                mProcess = Runtime.getRuntime().exec(mCmd);
                is = mProcess.getInputStream();
                os = new FileOutputStream(mOutDir);
                byte[] buffer = new byte[1024 * 1024];
                int len;
                while (mRunning && (len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mProcess != null) {
                    mProcess.destroy();
                    mProcess = null;
                }
                Utils.closeStream(is);
                Utils.closeStream(os);
            }
            mRunning = false;
            mLogTrace.i("run", "end: " + mTaskName);
        }

        @Override
        public void stop() {
            mRunning = false;
        }

        @Override
        public boolean isRunning() {
            return mRunning;
        }

        @Override
        public String getTaskName() {
            return mTaskName;
        }
    }

    private class AnrSaveTask implements Task {

        private static final String ANR_PATH = "/data/anr/";
        private static final long REPEAT_TIME = 60 * 1000;

        private boolean mRunning;
        private File mOutDir;
        private String mTaskName;

        AnrSaveTask(File dir, String taskName) {
            mOutDir = dir;
            mTaskName = taskName;
        }

        @Override
        public void run() {
            mLogTrace.i("run", "start: " + mTaskName);
            mRunning = true;
            try {
                File anrDir = new File(ANR_PATH);
                if (anrDir.exists() && !FileUtils.fileEquals(anrDir, mOutDir)) {
                    FileUtils.fileCopy(anrDir, mOutDir);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRunning = false;
            mLogTrace.i("run", "end: " + mTaskName);
        }

        @Override
        public void stop() {
            mRunning = false;
        }

        @Override
        public boolean isRunning() {
            return mRunning;
        }

        @Override
        public String getTaskName() {
            return mTaskName;
        }
    }

    private class CrashSaveTask implements Task {

        private static final String CRASH_PATH = "/data/crash/";

        private boolean mRunning;
        private File mOutDir;
        private String mTaskName;

        CrashSaveTask(File dir, String taskName) {
            mOutDir = dir;
            mTaskName = taskName;
        }

        @Override
        public void run() {
            mLogTrace.i("run", "start: " + mTaskName);
            mRunning = true;
            try {
                File crashDir = new File(CRASH_PATH);
                File outDir = new File(mOutDir, "crash");
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                if (crashDir.exists()) {
                    FileUtils.fileCopy(crashDir, outDir);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRunning = false;
            mLogTrace.i("run", "end: " + mTaskName);
        }

        @Override
        public void stop() {
            mRunning = false;
        }

        @Override
        public boolean isRunning() {
            return mRunning;
        }

        @Override
        public String getTaskName() {
            return mTaskName;
        }
    }

    interface Task extends Runnable {
        void stop();

        boolean isRunning();

        String getTaskName();
    }

}
