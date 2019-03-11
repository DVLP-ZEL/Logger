package com.skypine.elzhao.logger.utils;

import android.content.Context;
import android.widget.Toast;

import com.skypine.elzhao.logger.R;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Utils {

    private static LogTrace sLogTrace = new LogTrace(Constant.TAG, Utils.class.getSimpleName());

    public static void takeScreenShot(Context context){
        List<String> paths = StorageManager.getInstance(context).getVolumeListForMountPath();
        if (paths.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.no_u_disk), Toast.LENGTH_SHORT).show();
            return;
        }
        String path = paths.get(0);
        File dir = new File(path,"screencap");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            String savedPath = dir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".png";
            sLogTrace.i("takeScreenShot", "savedPath: " + savedPath);
            Process p = Runtime.getRuntime().exec("screencap -p " + savedPath);
            int status = p.waitFor();
            if (status == 0) {
                Toast.makeText(context, savedPath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.screenshot_failed), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeStream(Closeable io) {
        try {
            if (io != null) {
                io.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
