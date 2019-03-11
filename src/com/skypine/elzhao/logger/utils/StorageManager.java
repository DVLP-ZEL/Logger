package com.skypine.elzhao.logger.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * U盘工具类
 * 功能：
 * 1.判断U盘是否连接
 * 2.获取连接U盘列表
 * 3.处理路径显示问题
 *
 * @author administrator
 * @version [版本号]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class StorageManager {

    private static final String TAG = "MediaModule";

    private static StorageManager managerUtil;
    private static android.os.storage.StorageManager storageManager;
    //路径列表
    private List<String> unMountPaths = new ArrayList<>();

    //构造函数
    private StorageManager(Context context) {
        storageManager = (android.os.storage.StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }

    public static StorageManager getInstance(Context context) {
        if (managerUtil == null) {
            managerUtil = new StorageManager(context);
        }
        return managerUtil;
    }

    //添加mount路径
    public void setUnMountPath(String path) {
        Log.d(TAG, "setUnMountPath :" + path);
        unMountPaths.add(path);
    }

    public void setMountPath(String path) {
        Log.i(TAG, "setMountPath :" + path);
        if (unMountPaths.isEmpty()) {
            return;
        }
        //循环删除
        for (int i = 0; i < unMountPaths.size(); i++) {
            if (unMountPaths.get(i).equals(path)) {
                unMountPaths.remove(i);
            }
        }
        Log.d(TAG, "setMountPath :" + unMountPaths.size());
    }

    //获取U盘路径
    public List<String> getVolumeListForMountPath() {
        List<String> mountPaths = new ArrayList<>();
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList =
                    storageManager.getClass().getMethod("getVolumeList", paramClasses);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
            if (invokes != null) {
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath", new Class[0]);
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    Method getVolumeState =
                            storageManager.getClass().getMethod("getVolumeState", String.class);
                    String state = (String) getVolumeState.invoke(storageManager, path);
                    // Method getUserLabel = obj.getClass().getMethod(
                    // "getUserLabel", new Class[0]);
                    // String userLabel = (String) getUserLabel.invoke(obj,
                    // new Object[0]);
                    if ("/storage/emulated/0".equals(path)) {
                        continue;
                    }
                    Log.d(TAG, "getVolumeListForMountPath :" + path + ",state =" + state +
                            " ,unMountPaths = " + +unMountPaths.size());
                    if ("mounted".equals(state)) {
                        mountPaths.add(path);
                        for (int k = 0; k < unMountPaths.size(); k++) {
                            if (unMountPaths.get(k).equals(path)) {
                                mountPaths.remove(k);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mountPaths;
    }
}
