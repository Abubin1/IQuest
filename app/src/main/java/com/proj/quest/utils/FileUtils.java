package com.proj.quest.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static File getFile(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        try {
            // Создаем временный файл в кеше приложения
            String fileName = "upload_temp_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(fileName, ".tmp", context.getCacheDir());
            tempFile.deleteOnExit();

            // Копируем содержимое из Uri во временный файл
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {

                if (inputStream == null) {
                    return null;
                }

                byte[] buffer = new byte[4 * 1024]; // 4k buffer
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return tempFile;
        } catch (Exception e) {
            Log.e("FileUtils", "Failed to create temp file from Uri", e);
            return null;
        }
    }

    public static String getPath(Context context, Uri uri) {
        if (uri == null) return null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Для новых версий Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return "/storage/emulated/0/" + split[1];
                }
            }
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(column_index);
                    if (!TextUtils.isEmpty(path)) return path;
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return null;
    }

    public static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) return null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new File(uri.getPath());
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                String fileName = "temp_" + System.currentTimeMillis();
                File tempFile = new File(context.getCacheDir(), fileName);
                OutputStream outputStream = new FileOutputStream(tempFile);
                byte[] buf = new byte[4096];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
                return tempFile;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
} 