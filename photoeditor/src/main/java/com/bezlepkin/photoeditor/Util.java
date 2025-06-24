package com.bezlepkin.photoeditor;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Util {
    private final static String FILENAME_PREFIX = "photo_editor";

    public static File createExternalFile(Context context) throws IOException {
        String filename = FILENAME_PREFIX + "_" + String.valueOf(System.currentTimeMillis());
        return new File(context.getExternalFilesDir(null), filename + ".jpg");
    }

    public static File createExternalTempFile(Context context) throws IOException {
        return File.createTempFile("photo_editor_", ".jpg", context.getExternalCacheDir());
    }

    public static File createTempFile(Context context) throws IOException {
        return File.createTempFile("photo_editor_", ".jpg", context.getCacheDir());
    }

    public static void copyFile(File source, File destination) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(source);
             FileOutputStream outputStream = new FileOutputStream(destination)
        ) {
            FileChannel sourceChannel = inputStream.getChannel();
            FileChannel destinationChannel = outputStream.getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

            sourceChannel.close();
            destinationChannel.close();
        } catch (IOException e) {
            Log.d("Error copy file: ", e.getMessage());
        }
    }

    public static float dpToPx(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }
}
