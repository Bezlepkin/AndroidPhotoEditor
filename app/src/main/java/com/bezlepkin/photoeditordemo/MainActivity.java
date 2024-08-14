package com.bezlepkin.photoeditordemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.bezlepkin.photoeditor.PhotoEditorActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private String filepath;
    private static final int CAMERA_PERM_CODE = 101;
    private static final int REQUEST_CAMERA_CAPTURE = 111;
    private final String TAG = this.getClass().getSimpleName();
    private static final String PHOTO_NAME_PREFIX = "photo_editor";

    public void dispatchTakePhotoIntent() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = createImageFile();
            filepath = file.getAbsolutePath();
            Uri fileURI = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI);
            startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePhotoIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Window window = this.getWindow();
        // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissionAndDispatchTakePhotoIntent();
            }
        });
    }

    private void askCameraPermissionAndDispatchTakePhotoIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePhotoIntent();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, PhotoEditorActivity.class);
            intent.putExtra("imagePath", filepath);
            startActivity(intent);
        }
    }

    private File createImageFile() throws IOException {
        // a standardized name for the photo so every photo taken by your app have it in common, plus the current time in milli second to make unique
        String filename = PHOTO_NAME_PREFIX + "_" + String.valueOf(System.currentTimeMillis());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(filename, ".jpg", storageDir);
    }
}