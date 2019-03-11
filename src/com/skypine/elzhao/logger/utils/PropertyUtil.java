package com.skypine.elzhao.logger.utils;

import java.lang.reflect.Method;

/**
 * 读写工具
 * &lt;功能详细描述&gt;
 *
 * @author administrator
 * @version [版本号]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class PropertyUtil {

    public static void setProperty(String key, int value) {
        setPropertyReflect(key, String.valueOf(value));
    }

    public static int getProperty(String key, int def) {
        String str = getPropertyReflect(key, String.valueOf(def));
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    public static void setProperty(String key, String value) {
        setPropertyReflect(key, value);
    }

    public static String getProperty(String key) {
        return getPropertyReflect(key, "");
    }

    public static void setProperty(String key, boolean value) {
        setPropertyReflect(key, String.valueOf(value));
    }

    public static boolean getProperty(String key, boolean def) {
        String str = getPropertyReflect(key, String.valueOf(def));
        if ("true".equals(str)) {
            return true;
        } else if ("false".equals(str)) {
            return false;
        } else {
            return def;
        }
    }

    private static String getPropertyReflect(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private static void setPropertyReflect(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}