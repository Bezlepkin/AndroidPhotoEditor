package com.bezlepkin.photoeditor;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.bezlepkin.photoeditor.Utils;
import com.bezlepkin.photoeditor.base.BaseActivity;
import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoCropperActivity extends BaseActivity {
    private String filepath;
    private CropImageView cropImageView;
    private ImageButton closeButton;
    private ImageButton applyButton;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);
                AlertDialog dialog = builder.create();
                 */
                close(Activity.RESULT_CANCELED, new Intent());
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    Bitmap bitmap = cropImageView.getCroppedImage();
                    File file = Utils.createTempFile(getApplicationContext());
                    FileOutputStream out = new FileOutputStream(file);

                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        intent.putExtra("filepath", file.getAbsolutePath());
                    }

                    close(Activity.RESULT_OK, intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}