package com.skypine.elzhao.logger.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.skypine.elzhao.logger.R;
import com.skypine.elzhao.logger.helper.LogcatHelper;
import com.skypine.elzhao.logger.utils.Constant;
import com.skypine.elzhao.logger.utils.FileUtils;
import com.skypine.elzhao.logger.utils.LogTrace;
import com.skypine.elzhao.logger.utils.PropertyUtil;
import com.skypine.elzhao.logger.utils.StorageManager;
import com.skypine.elzhao.logger.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoggerService extends Service {

	private LogTrace mLogTrace = new LogTrace(Constant.TAG, this.getClass().getSimpleName());

	public static final String EXTRA_LOG_CMD  = "extra.log.cmd";
	public static final String EXTRA_LOG_PATH = "extra.log.path";
	public static final int INVALID_CMD = -1;
	public static final int CMD_STOP_SAVE_LOG       = 0x1001;
	public static final int CMD_START_SAVE_LOG      = 0x1002;
	public static final int CMD_AUTO_SAVE_LOG       = 0x1003;
	public static final int CMD_AUTO_SAVE_ON        = 0x1004;
	public static final int CMD_AUTO_SAVE_OFF       = 0x1005;
	public static final int CMD_QUERY_AUTO_SAVE     = 0x1006;
	public static final int CMD_TAKE_SCREEN_SHOT    = 0x1007;
	public static final int CMD_AUTO_SAVE_LOG_PATH  = 0x1008;

	private Context mContext;

	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CMD_STOP_SAVE_LOG:
				stopSaveLog();
				break;
			case CMD_START_SAVE_LOG:
				startSaveLog(false);
				break;
			case CMD_AUTO_SAVE_LOG:
				startSaveLog(true);
				break;
			case CMD_AUTO_SAVE_ON:
				PropertyUtil.setProperty(Constant.KEY_AUTO_SAVE, true);
				Toast.makeText(mContext, mContext.getString(R.string.auto_save_on), Toast.LENGTH_SHORT).show();
				break;
			case CMD_AUTO_SAVE_OFF:
				PropertyUtil.setProperty(Constant.KEY_AUTO_SAVE, false);
				Toast.makeText(mContext, mContext.getString(R.string.auto_save_off), Toast.LENGTH_SHORT).show();
				break;
			case CMD_QUERY_AUTO_SAVE:
				boolean auto = PropertyUtil.getProperty(Constant.KEY_AUTO_SAVE, Constant.DEFAULT_AUTO_SAVE);
				Toast.makeText(mContext, String.valueOf(auto), Toast.LENGTH_SHORT).show();
				break;
			case CMD_TAKE_SCREEN_SHOT:
				Utils.takeScreenShot(mContext);
				break;
			case CMD_AUTO_SAVE_LOG_PATH:
				String path = (String) msg.obj;
				startSaveLog(path);
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	public void onCreate() {
		super.onCreate();
		mLogTrace.i("onCreate");
		mContext = getApplicationContext();
		registerBroadcastReceiver();
	}

	private void registerBroadcastReceiver() {
		IntentFilter mediaFilter = new IntentFilter();
		mediaFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mediaFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		mediaFilter.addDataScheme("file");
		mContext.registerReceiver(mReceiver, mediaFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int cmd = INVALID_CMD;
		String path = null;
		if (intent != null) {
			cmd = intent.getIntExtra(EXTRA_LOG_CMD, INVALID_CMD);
			path = intent.getStringExtra(EXTRA_LOG_PATH);
		}
		mLogTrace.i("onStartCommand", "cmd: " + cmd + " - path: " + path);
		mHandler.sendMessage(Message.obtain(mHandler, cmd, path));
		return START_STICKY;
	}

	private void startSaveLog(boolean auto) {
		boolean isRunning = LogcatHelper.getInstance().isRunning();
		mLogTrace.i("startSaveLog", "isRunning: " + isRunning);
		if (isRunning) {
			if (!auto) {
				Toast.makeText(mContext, mContext.getString(R.string.log_running), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		List<String> paths = StorageManager.getInstance(mContext).getVolumeListForMountPath();
		mLogTrace.i("startSaveLog", "paths: " + paths.size());
		if (paths.size() == 0) {
			if (!auto) {
				Toast.makeText(mContext, mContext.getString(R.string.no_usb), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
		String timeStamp = format.format(new Date(System.currentTimeMillis()));// 20151003234131
		File dir;
		if (auto) {
			dir = new File(paths.get(0), "/log/auto/");
			if (dir.exists()) {
				FileUtils.deleteFile(dir);
			}
		} else {
			dir = new File(paths.get(0), "/log/" + timeStamp);
		}
		if (dir.exists() || (!dir.exists() && dir.mkdirs())) {
			mLogTrace.i("startLog", "path: " + dir.getAbsolutePath());
			LogcatHelper.getInstance().start(dir);
			if (!auto) {
				Toast.makeText(mContext, mContext.getString(R.string.log_start), Toast.LENGTH_SHORT).show();
			}
		} else if (!auto) {
			Toast.makeText(mContext, mContext.getString(R.string.make_log_dir_error), Toast.LENGTH_SHORT).show();
		}
	}

	private void startSaveLog(String path) {
		if (path == null) {
			return;
		}
		boolean isRunning = LogcatHelper.getInstance().isRunning();
		mLogTrace.i("startSaveLog", "isRunning: " + isRunning);
		if (isRunning) {
			return;
		}
		File dir = new File(path, "/log/");
		if (dir.exists()) {
			FileUtils.deleteFile(dir);
		}
		if (dir.exists() || dir.mkdirs()) {
			mLogTrace.i("startLog", "path: " + dir.getAbsolutePath());
			//Toast.makeText(mContext, "dir: " + dir.getAbsolutePath() + " - " + dir.exists(), Toast.LENGTH_SHORT).show();
			LogcatHelper.getInstance().start(dir);
		}
	}

	private void stopSaveLog() {
		mLogTrace.i("stopLog");
		LogcatHelper.getInstance().stop();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLogTrace.i("onDestroy");
		mContext.unregisterReceiver(mReceiver);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			mLogTrace.i("onReceive", "action: " + action);
		}
	};
}
