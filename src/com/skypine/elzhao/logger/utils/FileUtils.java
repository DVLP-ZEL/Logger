package com.skypine.elzhao.logger.utils;

import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static void fileCopy(File copyDir, File targetDir) throws IOException {
        if (copyDir == null || !copyDir.exists() || targetDir == null || !targetDir.exists()) {
            return;
        }
        File[] files = copyDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            File file = new File(targetDir, f.getName());
            if (file.exists()) {
                deleteFile(file);
            }
            if (f.isDirectory()) {
                file.mkdirs();
                fileCopy(f, file);
            } else {
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(f);
                    fos = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                } finally {
                    closeStream(fis);
                    closeStream(fos);
                }
            }
        }
    }

    public static void deleteFile(File file) {
        if (file != null) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile != null && childFile.length > 0) {
                    for (File f : childFile) {
                        deleteFile(f);
                    }
                }
                file.delete();
            }
        }
    }

    private static void closeStream(Closeable io) throws IOException {
        if (io != null) {
            io.close();
        }
    }

    public static int listFiles(File dir) {
        int count = 0;
        if (dir != null) {
            File[] fs = dir.listFiles();
            for (File f : fs) {
                if (f.isDirectory()) {
                    count += listFiles(f);
                } else {
                    count ++;
                }
            }
        }
        return count;
    }

    public static boolean fileEquals(File copyDir, File targetDir) {
        if (copyDir != null && targetDir != null && copyDir.isDirectory() && targetDir.isDirectory()) {
            File[] copyFiles = copyDir.listFiles();
            for (File file:copyFiles) {
                File targetFile = new File(targetDir, file.getName());
                if (!targetFile.exists() || file.length() != targetFile.length()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static long getDirSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return 0;
        }
        long size = 0;
        File[] fs = dir.listFiles();
        for (File f:fs) {
            size += f.length();
        }
        return size;
    }

    public static void clearDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return;
        }
        File[] fs = dir.listFiles();
        for (File f:fs) {
            f.delete();
        }
    }

    /**
     * 创建目录（可以是多个）
     *
     * @param filePath 目录路径
     * @return 如果路径为空时，返回false；如果目录创建成功，则返回true，否则返回false
     */
    public static boolean makeDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File folder = new File(filePath);
        return !(folder.exists() && folder.isDirectory()) && folder.mkdirs();
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 文件目录
     * @param content  要写入的内容
     * @param append   如果为 true，则将数据写入文件末尾处，而不是写入文件开始处
     * @return 写入成功返回true， 写入失败返回false
     * @throws IOException
     */
    public static boolean writeFile(String filePath, String content, boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            createFile(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
            return true;
        } finally {
            closeStream(fileWriter);
        }
    }

    /**
     * 创建文件
     *
     * @param path 文件的绝对路径
     * @return
     */
    public static boolean createFile(String path) {
        return !TextUtils.isEmpty(path) && createFile(new File(path));
    }

    /**
     * 创建文件
     *
     * @param file
     * @return 创建成功返回true
     */
    public static boolean createFile(File file) {
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
