package com.bezlepkin.photoeditor;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.base.BaseActivity;
import com.bezlepkin.photoeditor.Constant;
import com.bezlepkin.photoeditor.adapters.ColorPickerAdapter;
import com.bezlepkin.photoeditor.helpers.InsetsWithKeyboardAnimationCallbackHelper;
import com.bezlepkin.photoeditor.helpers.InsetsWithKeyboardCallbackHelper;
import com.bezlepkin.photoeditorsdk.BrushDrawingView;
import com.bezlepkin.photoeditorsdk.PhotoEditorSDK;
import com.bezlepkin.photoeditorsdk.OnPhotoEditorSDKListener;
import com.bezlepkin.photoeditorsdk.ViewType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoEditorActivity extends BaseActivity implements OnPhotoEditorSDKListener {
    private ModeType activeMode;
    private File outputFile;
    int textColor = Color.WHITE;
    int textColorIndex = 0;
    private String currentFilepath;
    private ImageView imageView;
    private RelativeLayout imageLayout;
    private ImageButton closeButton;
    private ImageButton cropButton;
    private ImageButton drawButton;
    private ImageButton textButton;
    private ImageButton cancelButton;
    private ImageButton applyButton;
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

        String filepath = extras.getString("imagePath");
        ArrayList<Integer> colors = extras.getIntegerArrayList("colors");
        initImageView();
        // buttons
        closeButton = findViewById(R.id.close_button);
        cropButton = findViewById(R.id.crop_button);
        drawButton = findViewById(R.id.draw_button);
        textButton = findViewById(R.id.text_button);
        cancelButton = findViewById(R.id.cancel_button);
        applyButton = findViewById(R.id.apply_button);
        saveButton = findViewById(R.id.save_button);
        // controls
        actionControls = findViewById(R.id.action_controls_layout);
        modeControls = findViewById(R.id.mode_controls_layout);
        bottomControls = findViewById(R.id.bottom_controls_layout);
        // drawing views
        BrushDrawingView brushDrawingView = findViewById(R.id.drawing_view);
        drawingViewColorPickerRecyclerView = findViewById(R.id.drawing_view_color_picker_recycler_view);

        RelativeLayout deleteRelativeLayout = findViewById(R.id.delete_rl);

        setCurrentFilepath(filepath);
        setImage(filepath);
        initColors(colors);

        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(PhotoEditorActivity.this).parentView(imageLayout).childView(imageView).deleteView(deleteRelativeLayout) // add the deleted view that will appear during the movement of the views
                .brushDrawingView(brushDrawingView) // add the brush drawing view that is responsible for drawing on the image view
                .buildPhotoEditorSDK(); // build photo editor sdk

        photoEditorSDK.setOnPhotoEditorSDKListener(this);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(Activity.RESULT_CANCELED, new Intent());
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginCropping();
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDrawingMode(true);
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeMode = ModeType.TEXT;
                openAddTextPopupWindow("", textColor);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeMode == ModeType.DRAW) {
                    updateDrawingMode(false);
                } else if (activeMode == ModeType.TEXT) {
                    setEditMode(false);
                }

                photoEditorSDK.clearAllViews();
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeMode == ModeType.DRAW) {
                    saveDrawingResult();
                    updateDrawingMode(false);
                } else if (activeMode == ModeType.TEXT) {
                    saveDrawingResult();
                    photoEditorSDK.clearAllViews();
                    setEditMode(false);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResultWithReturn();
            }
        });

        onKeyboardShowListener(this);
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

    public void onKeyboardShowListener(final Activity activity) {
        Context context = getApplication().getApplicationContext();
        final View view = this.findViewById(android.R.id.content);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                view.getWindowVisibleDisplayFrame(rect);
                int rootHeight = view.getRootView().getHeight();
                int diffHeight = rootHeight - rect.bottom;
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                float scale = metrics.density;
                int height = (int) (diffHeight / scale);

                if (diffHeight > rootHeight * 0.15) {
                    Log.d("Keyboard", "Keyboard is open");
                } else {
                    Log.d("Keyboard", "Keyboard is closed");
                }
            }
        });

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

    private void initColors(ArrayList<Integer> colors) {
        if (!colors.isEmpty()) {
            colorPickerColors = colors;
        } else {
            colorPickerColors = Constant.getDefaultColors(this);
        }
    }

    private void setImage(String filepath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        // imageView.setImageURI(Uri.fromFile(new File(filepath)));
        imageView.setImageBitmap(bitmap);
    }

    private void beginCropping() {
        Intent intent = new Intent(this, PhotoCropperActivity.class);
        intent.putExtra("filepath", currentFilepath);
        // noinspection deprecation
        startActivityForResult(intent, CROPPER_REQUEST_CODE);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateDrawingMode(Boolean drawingMode) {
        int colorIndex = !colorPickerColors.contains(2) ? 0 : 2;
        photoEditorSDK.setBrushDrawingMode(drawingMode);
        photoEditorSDK.setBrushColor(colorPickerColors.get(colorIndex));

        if (drawingMode) {
            activeMode = ModeType.DRAW;
            modeControls.setVisibility(View.GONE);
            bottomControls.setVisibility(View.GONE);
            setActionControlsVisibility(View.VISIBLE);
            drawingViewColorPickerRecyclerView.setVisibility(View.VISIBLE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            drawingViewColorPickerRecyclerView.setLayoutManager(layoutManager);
            drawingViewColorPickerRecyclerView.setHasFixedSize(true);
            ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(this, colorPickerColors);
            colorPickerAdapter.setActiveColorIndex(colorIndex);

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

        final TextAdditionalPopupWindow textAdditionalPopup = new TextAdditionalPopupWindow(
                this,
                (color != null) ? color : colorPickerColors.get(textColorIndex),
                colorPickerColors
        );

        textAdditionalPopup.show();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        textAdditionalPopup.setOnApplyClickListener(new TextAdditionalPopupWindow.OnApplyClickListener() {
            @Override
            public void onClick(String text, int color) {
                saveAddedTextResult(text, color);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textAdditionalPopup.getContentView().getWindowToken(), 0);
                textAdditionalPopup.dismiss();
            }
        });

        textAdditionalPopup.setOnColorPickerClickListener(new TextAdditionalPopupWindow.OnColorPickerClickListener() {
            @Override
            public void onClick(int colorCode) {
                textColorIndex = colorPickerColors.indexOf(colorCode);
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

    private void saveAddedTextResult(String text, int colorCode) {
        photoEditorSDK.addText(text, colorCode);
        setEditMode(true);
        // photoEditorSDK.clearAllViews();
    }

    private void saveResultWithReturn() {
        try {
            if (outputFile == null) {
                outputFile = Util.createExternalFile(getApplicationContext());
            }

            Util.copyFile(new File(currentFilepath), outputFile);

            Intent intent = new Intent();
            intent.putExtra("filepath", outputFile.getAbsolutePath());

            close(Activity.RESULT_OK, intent);
        } catch (IOException e) {
            Log.d(TAG, "Error save output image ", e);
        }
    }

    private void initImageView() {
        imageView = findViewById(R.id.image_view);
        imageLayout = findViewById(R.id.image_layout);
        final View parentView = findViewById(R.id.parent_layout);

        final Window window = getWindow();
        final InsetsWithKeyboardCallbackHelper insetsWithKeyboardCallbackHelper = new InsetsWithKeyboardCallbackHelper(window);
        final WindowInsetsAnimationCompat.Callback insetsWithKeyboardAnimationCallbackHelper = new InsetsWithKeyboardAnimationCallbackHelper(parentView);

        ViewCompat.setOnApplyWindowInsetsListener(parentView, insetsWithKeyboardCallbackHelper);
        ViewCompat.setWindowInsetsAnimationCallback(parentView, insetsWithKeyboardAnimationCallbackHelper);
    }

    protected void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            modeControls.setVisibility(View.GONE);
            bottomControls.setVisibility(View.GONE);
            setActionControlsVisibility(View.VISIBLE);
        } else {
            modeControls.setVisibility(View.VISIBLE);
            bottomControls.setVisibility(View.VISIBLE);
            setActionControlsVisibility(View.GONE);
        }
    }
}