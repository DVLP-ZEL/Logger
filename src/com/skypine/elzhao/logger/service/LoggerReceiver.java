package com.skypine.elzhao.logger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.skypine.elzhao.logger.utils.Constant;
import com.skypine.elzhao.logger.utils.LogTrace;
import com.skypine.elzhao.logger.utils.PropertyUtil;

public class LoggerReceiver extends BroadcastReceiver {

    private LogTrace mLogTrace = new LogTrace(Constant.TAG, this.getClass().getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String host = intent.getData() == null ? null : intent.getData().getHost();
        mLogTrace.i("onReceive", "action: " + action + " - host: " + host);
        if (Constant.ACTION_SECRET_CODE.equals(action)) {
            if ("369369".equals(host)) {
                startServiceCmd(context, LoggerService.CMD_START_SAVE_LOG);
            } else if ("963963".equals(host)) {
                startServiceCmd(context, LoggerService.CMD_STOP_SAVE_LOG);
            } else if ("123123".equals(host)) {
                startServiceCmd(context, LoggerService.CMD_AUTO_SAVE_ON);
            } else if ("321321".equals(host)) {
                startServiceCmd(context, LoggerService.CMD_AUTO_SAVE_OFF);
            } else if ("123321".equals(host)) {
                startServiceCmd(context, LoggerService.CMD_QUERY_AUTO_SAVE);
            }
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            boolean auto = PropertyUtil.getProperty(Constant.KEY_AUTO_SAVE, Constant.DEFAULT_AUTO_SAVE);
            String path = intent.getData().getPath();
            mLogTrace.i("onReceive", "auto: " + auto + " - path: " + path);
			if (auto && path.contains("/mnt/udisk")) {
                startServiceCmd(context, LoggerService.CMD_AUTO_SAVE_LOG_PATH, path);
            }
        } else if (Constant.ACTION_SCREEN_SHOT.equals(action)) {
            startServiceCmd(context, LoggerService.CMD_TAKE_SCREEN_SHOT);
        }
    }

    private void startServiceCmd(Context context, int cmd) {
    	startServiceCmd(context, cmd, null);
    }
    
    private void startServiceCmd(Context context, int cmd, String path) {
        Intent service = new Intent(context, LoggerService.class);
        service.putExtra(LoggerService.EXTRA_LOG_CMD, cmd);
        service.putExtra(LoggerService.EXTRA_LOG_PATH, path);
        context.startService(service);
    }
}
