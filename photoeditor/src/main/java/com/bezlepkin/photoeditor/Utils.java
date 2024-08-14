package com.bezlepkin.photoeditor;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class Utils {
    private final static String FILENAME_PREFIX = "photo_editor";

    public static File createExternalFile(Context context) throws IOException {
        String filename = FILENAME_PREFIX + "_" + String.valueOf(System.currentTimeMillis());
        return new File(context.getExternalFilesDir(null).getAbsolutePath(), filename + ".jpg");
    }

    public static File createExternalTempFile(Context context) throws IOException {
        return File.createTempFile("photo_editor_", ".jpg", context.getExternalCacheDir());
    }

    public static File createTempFile(Context context) throws IOException {
        return File.createTempFile("photo_editor_", ".jpg", context.getCacheDir());
    }

    public static void copyFile(File source, File destination) throws IOException {
        /*
        if (!destination.exists()) {
            destination.createNewFile();
        }
           */
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
}
