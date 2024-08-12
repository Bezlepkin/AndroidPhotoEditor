package com.bezlepkin.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoCropperActivity extends AppCompatActivity {
    private String filepath;
    private CropImageView cropImageView;
    private ImageButton closeButton;
    private ImageButton applyButton;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setting status bar and navigation bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        setContentView(R.layout.activity_photo_cropper);

        cropImageView = findViewById(R.id.crop_image_view);
        closeButton = findViewById(R.id.close_button);
        applyButton = findViewById(R.id.apply_button);

        Bundle extras = getIntent().getExtras();
        filepath = extras.getString("filepath");
        File file = new File(filepath);

        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            cropImageView.setImageBitmap(bitmap);
        }


        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    Bitmap bitmap = cropImageView.getCroppedImage();
                    String filepath = createTempFileUri().getPath();
                    FileOutputStream out = new FileOutputStream(new File(filepath));

                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        intent.putExtra("filepath", filepath);
                    }

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Uri createTempFileUri() {
        Uri tempFileUri;

        try {
            tempFileUri = Uri.fromFile(File.createTempFile("photo_editor_", ".jpg", getCacheDir()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file for output image", e);
        }

        return tempFileUri;
    }
}