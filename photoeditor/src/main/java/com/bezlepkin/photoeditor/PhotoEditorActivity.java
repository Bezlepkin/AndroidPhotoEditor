package com.bezlepkin.photoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.base.BaseActivity;
import com.bezlepkin.photoeditorsdk.BrushDrawingView;
import com.bezlepkin.photoeditorsdk.PhotoEditorSDK;
import com.bezlepkin.photoeditorsdk.OnPhotoEditorSDKListener;
import com.bezlepkin.photoeditorsdk.ViewType;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

enum Mode {
    CROP,
    DRAW,
    TEXT
}

public class PhotoEditorActivity extends BaseActivity implements OnPhotoEditorSDKListener {
    private Mode activeMode;
    private File outputFile;
    int textColor = Color.WHITE;
    private String originFilepath;
    private String currentFilepath;
    private ImageView imageView;
    private RelativeLayout imageWrapLayout;
    private ImageButton closeButton;
    private ImageButton cropButton;
    private ImageButton drawButton;
    private ImageButton textButton;
    private ImageButton cancelButton;
    private ImageButton applyButton;
    private ImageButton shareButton;
    private Button saveButton;
    // controls
    private LinearLayout actionControls;
    private RelativeLayout modeControls;
    private LinearLayout bottomControls;
    private RecyclerView drawingViewColorPickerRecyclerView;
    // for draw
    private ArrayList<Integer> colorPickerColors;
    private TextView doneDrawingTextView;
    private PhotoEditorSDK photoEditorSDK;
    private ActivityResultLauncher cropActivityForResult;

    private final static int CROPPER_REQUEST_CODE = 1;
    private final static String FILENAME_PREFIX = "photo_editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_editor);

        Bundle extras = getIntent().getExtras();

        originFilepath = extras.getString("imagePath");
        setCurrentFilepath(originFilepath);

        imageView = findViewById(R.id.image_view);
        imageWrapLayout = findViewById(R.id.image_wrap_layout);
        // buttons
        closeButton = findViewById(R.id.close_button);
        cropButton = findViewById(R.id.crop_button);
        drawButton = findViewById(R.id.draw_button);
        textButton = findViewById(R.id.text_button);
        cancelButton = findViewById(R.id.cancel_button);
        applyButton = findViewById(R.id.apply_button);
        shareButton = findViewById(R.id.share_button);
        saveButton = findViewById(R.id.save_button);
        // controls
        actionControls = findViewById(R.id.action_controls_layout);
        modeControls = findViewById(R.id.mode_controls_layout);
        bottomControls = findViewById(R.id.bottom_controls_layout);
        // drawing views
        BrushDrawingView brushDrawingView = findViewById(R.id.drawing_view);
        drawingViewColorPickerRecyclerView = findViewById(R.id.drawing_view_color_picker_recycler_view);

        RelativeLayout deleteRelativeLayout = findViewById(R.id.delete_rl);

        setImage(originFilepath);
        // setup draw colors
        setupColorPickerColors();

        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(PhotoEditorActivity.this)
                .parentView(imageWrapLayout)
                .childView(imageView)
                .deleteView(deleteRelativeLayout) // add the deleted view that will appear during the movement of the views
                .brushDrawingView(brushDrawingView) // add the brush drawing view that is responsible for drawing on the image view
                .buildPhotoEditorSDK(); // build photo editor sdk

        photoEditorSDK.setOnPhotoEditorSDKListener(this);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginCropping();
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddTextPopupWindow("", textColor);
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDrawingMode(true);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeMode == Mode.DRAW) {
                    updateDrawingMode(false);
                } else if (activeMode == Mode.TEXT) {

                }
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeMode == Mode.DRAW) {
                    saveDrawingResult();
                    updateDrawingMode(false);
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile(currentFilepath);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // saveResultWithReturn();
                saveResultWithReturn2();
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onEditTextChangeListener(String text, int color) {
        // TODO: triggers text editing
        // openAddTextPopupWindow(text, color);
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {

    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onStartViewChangeListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onStartViewChangeListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onStartViewChangeListener");
                break;
            case TEXT:
                Log.i("TEXT", "onStartViewChangeListener");
                break;
        }
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CROPPER_REQUEST_CODE) {
            String filepath = data.getExtras().getString("filepath");
            setCurrentFilepath(filepath);
            setImage(currentFilepath);
        }
    }

    private void setupColorPickerColors() {
        colorPickerColors = new ArrayList<>();
        // the Material colors are used here https://materialui.co/colors
        colorPickerColors.add(ContextCompat.getColor(this, R.color.white));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.black));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.red));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.green));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.blue));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.yellow));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.pink));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.orange));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.brown));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.cyan));
        colorPickerColors.add(ContextCompat.getColor(this, R.color.purple));
    }

    private void setImage(String filepath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);

        imageView.setImageBitmap(bitmap);
    }

    private void beginCropping() {
        Intent intent = new Intent(this, PhotoCropperActivity.class);
        intent.putExtra("filepath", currentFilepath);
        // noinspection deprecation
        startActivityForResult(intent, CROPPER_REQUEST_CODE);
    }

    private void updateDrawingMode(Boolean drawingMode) {
        photoEditorSDK.setBrushDrawingMode(drawingMode);

        if (drawingMode) {
            activeMode = Mode.DRAW;
            modeControls.setVisibility(View.GONE);
            bottomControls.setVisibility(View.GONE);
            setActionControlsVisibility(View.VISIBLE);
            drawingViewColorPickerRecyclerView.setVisibility(View.VISIBLE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(PhotoEditorActivity.this, LinearLayoutManager.HORIZONTAL, false);
            drawingViewColorPickerRecyclerView.setLayoutManager(layoutManager);
            drawingViewColorPickerRecyclerView.setHasFixedSize(true);
            ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(PhotoEditorActivity.this, colorPickerColors);
            colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
                @Override
                public void onColorPickerClickListener(int colorCode) {
                    photoEditorSDK.setBrushColor(colorCode);
                }
            });

            drawingViewColorPickerRecyclerView.setAdapter(colorPickerAdapter);
        } else {
            activeMode = null;
            modeControls.setVisibility(View.VISIBLE);
            bottomControls.setVisibility(View.VISIBLE);
            setActionControlsVisibility(View.GONE);
            drawingViewColorPickerRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setCurrentFilepath(String filepath) {
        currentFilepath = filepath;
    }

    private void setActionControlsVisibility(Integer visibility) {
        actionControls.setVisibility(visibility);
    }

    private void openAddTextPopupWindow(String text, Integer color) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View textAdditionalPopupView = inflater.inflate(R.layout.text_additional_popup, null);

        final EditText addTextEditText = textAdditionalPopupView.findViewById(R.id.text_input);
        addTextEditText.setTextColor(color != null ? color : textColor);
        addTextEditText.setHintTextColor(Color.parseColor("#40FFFFFF"));
        addTextEditText.requestFocus();

        ImageButton applyButton = textAdditionalPopupView.findViewById(R.id.apply_button);
        RecyclerView addTextColorPickerRecyclerView = textAdditionalPopupView.findViewById(R.id.add_text_color_picker_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PhotoEditorActivity.this, LinearLayoutManager.HORIZONTAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(PhotoEditorActivity.this, colorPickerColors);
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int color) {
                textColor = color;
                addTextEditText.setTextColor(textColor);
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);
        /*
        if (stringIsNotEmpty(text)) {
            addTextEditText.setText(text);
            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCode);
        }
        */
        final PopupWindow pop = new PopupWindow(PhotoEditorActivity.this);
        pop.setContentView(textAdditionalPopupView);
        pop.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setFocusable(true);
        pop.setBackgroundDrawable(null);
        pop.showAtLocation(textAdditionalPopupView, Gravity.TOP, 0, 0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAddedTextResult(addTextEditText.getText().toString(), textColor);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                pop.dismiss();
            }
        });
    }

    private void saveDrawingResult() {
        try {
            Uri tempFileUri = Uri.fromFile(File.createTempFile("photo_editor_", ".jpg", getCacheDir()));
            String[] filepathArr = tempFileUri.getPath().split("/");
            String filename = filepathArr[filepathArr.length - 1];
            String filepath = photoEditorSDK.saveImage("", filename);
            setCurrentFilepath(filepath);
            setImage(filepath);
        } catch (IOException e) {
            Log.e(TAG, "Error saving a drawing mode", e);
        }
    }

    private void saveAddedTextResult(String text, int color) {
        photoEditorSDK.addText(text, color);
    }

    private void saveResultWithReturn() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        imageWrapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    String filename = FILENAME_PREFIX + "_" + String.valueOf(System.currentTimeMillis());
                    File file = new File(getExternalFilesDir(null).getAbsolutePath(), filename + ".jpg");

                    Uri tempFileUri = Uri.fromFile(File.createTempFile("photo_editor_", ".jpg", getCacheDir()));
                    String[] filepathArr = tempFileUri.getPath().split("/");
                    String filename2 = filepathArr[filepathArr.length - 1];
                    String filepath = photoEditorSDK.saveImage("", filename2);

                    Intent intent = new Intent();
                    intent.putExtra("filepath", filepath);

                    setResult(Activity.RESULT_OK, intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    finish();
                }
            }
        });

        imageWrapLayout.setLayoutParams(layoutParams);
    }

    private void saveResultWithReturn2() {
        try {
            if (outputFile == null) {
                outputFile = Utils.createExternalFile(getApplicationContext());
            }

            Utils.copyFile(new File(currentFilepath), outputFile);

            Intent intent = new Intent();
            intent.putExtra("filepath", outputFile.getAbsolutePath());
            setResult(Activity.RESULT_OK, intent);
            finish();
        } catch (IOException e) {
            Log.d(TAG, "Error save output image ", e);
        }
    }

    private void shareFile(String filepath) {
        try {
            File file = Utils.createExternalTempFile(getApplicationContext());
            FileOutputStream stream = new FileOutputStream(file);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();

            Context context = getApplicationContext();
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            startActivity(Intent.createChooser(intent, "Share..."));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createOutputFile() {
        String filename = FILENAME_PREFIX + "_" + String.valueOf(System.currentTimeMillis());
        outputFile = new File(getExternalFilesDir(null).getAbsolutePath(), filename + ".jpg");
        Log.d(TAG, "___482 " + outputFile);
        // /storage/emulated/0/Android/data/com.bezlepkin.photoeditordemo/files/photo_editor_1723576640202.jpg

    }

    private File saveOutputImage(File outputFile) throws IOException {
        try {
            FileOutputStream stream = new FileOutputStream(outputFile);
            Bitmap bitmap = BitmapFactory.decodeFile(currentFilepath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();

            return outputFile;
        } catch (IOException e) {
            Log.e(TAG, "Error saving an output image", e);
            throw new IOException();
        }
    }
}