/*
 * Copyright 2025 Igor Bezlepkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bezlepkin.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import android.annotation.SuppressLint;

import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.animator.FragmentHeightAnimator;
import com.bezlepkin.photoeditor.base.BaseActivity;
import com.bezlepkin.photoeditor.adapters.ColorPickerAdapter;
import com.bezlepkin.photoeditorsdk.BrushDrawingView;
import com.bezlepkin.photoeditorsdk.PhotoEditorSDK;
import com.bezlepkin.photoeditorsdk.OnPhotoEditorSDKListener;
import com.bezlepkin.photoeditorsdk.ViewType;
import com.bezlepkin.photoeditor.databinding.ActivityPhotoEditorBinding;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PhotoEditorActivity extends BaseActivity implements OnPhotoEditorSDKListener {
    private ModeType activeMode;
    private File outputFile;
    int textColor = Color.WHITE;
    int textColorIndex = 0;
    private String currentFilepath;
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
    private PhotoEditorSDK photoEditorSDK;
    private ActivityResultLauncher cropActivityForResult;

    private final static int CROPPER_REQUEST_CODE = 1;
    private final static String FILENAME_PREFIX = "photo_editor";
    private ActivityPhotoEditorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this line allows your application to draw under the system bars (status bar, nav bar)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        String filepath = extras.getString("imagePath");

        ArrayList<Integer> colors = extras.getIntegerArrayList("colors");
        if (colors == null) {
            colors = new ArrayList<>();
        }
        // Buttons
        closeButton = findViewById(R.id.close_button);
        cropButton = findViewById(R.id.crop_button);
        drawButton = findViewById(R.id.draw_button);
        textButton = findViewById(R.id.text_button);
        cancelButton = findViewById(R.id.cancel_button);
        applyButton = findViewById(R.id.apply_button);
        saveButton = findViewById(R.id.save_button);
        // Controls
        actionControls = findViewById(R.id.action_controls_layout);
        modeControls = findViewById(R.id.mode_controls_layout);
        bottomControls = findViewById(R.id.bottom_controls_layout);
        // Drawing views
        BrushDrawingView brushDrawingView = findViewById(R.id.drawing_view);
        drawingViewColorPickerRecyclerView = findViewById(R.id.drawing_view_color_picker_recycler_view);

        RelativeLayout deleteRelativeLayout = findViewById(R.id.delete_rl);

        setCurrentFilepath(filepath);
        setImage(filepath);
        initColors(colors);


        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(PhotoEditorActivity.this)
                .parentView(binding.canvasLayout)
                .childView(binding.imageView)
                .deleteView(deleteRelativeLayout) // add the deleted view that will appear during the movement of the views
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
                runTypingMode(textColor);
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

        setupViewListeners();
        setupWindowInsetsListeners();
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
    public void onStopViewChangeListener(ViewType viewType) {}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CROPPER_REQUEST_CODE) {
            String filepath = Objects.requireNonNull(data.getExtras()).getString("filepath");
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
        Glide.with(this)
                .load(new File(filepath))
                .placeholder(binding.imageView.getDrawable())
                .dontAnimate()
                .into(binding.imageView);
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
            modeControls.setVisibility(View.INVISIBLE);
            bottomControls.setVisibility(View.INVISIBLE);
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
            setActionControlsVisibility(View.INVISIBLE);
            drawingViewColorPickerRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setCurrentFilepath(String filepath) {
        currentFilepath = filepath;
    }

    private void setActionControlsVisibility(Integer visibility) {
        actionControls.setVisibility(visibility);
    }

    private void saveDrawingResult() {
        try {
            Uri tempFileUri = Uri.fromFile(File.createTempFile("photo_editor_", ".jpg", getCacheDir()));
            String[] filepathArr = Objects.requireNonNull(tempFileUri.getPath()).split("/");
            String filename = filepathArr[filepathArr.length - 1];
            String savedFilePath = photoEditorSDK.saveImage("", filename);

            if (savedFilePath == null || savedFilePath.isEmpty()) {
                Log.e(TAG, "PhotoEditorSDK failed to save the image.");
                return;
            }

            File savedFile = new File(savedFilePath);
            if (!savedFile.exists()) {
                Log.e(TAG, "File does not exist at the path returned by SDK: " + savedFilePath);
                return;
            }

            setCurrentFilepath(savedFilePath);
            setImage(savedFilePath);
        } catch (IOException e) {
            Log.e(TAG, "Error saving a drawing mode", e);
        }
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

    protected void runTypingMode(Integer color) {
        activeMode = ModeType.TEXT;
        TextAdditionalFragment textAdditionalFragment = binding.textAdditionalFragment.getFragment();
        textAdditionalFragment.setup(
                this,
                (color != null) ? color : colorPickerColors.get(textColorIndex),
                colorPickerColors
        );

        textAdditionalFragment.setOnApplyClickListener(new TextAdditionalFragment.OnApplyClickListener() {
            @Override
            public void onClick(String text, int color) {
                saveTypingModeResult(text, color);
            }
        });

        textAdditionalFragment.setOnColorPickerClickListener(new TextAdditionalFragment.OnColorPickerClickListener() {
            @Override
            public void onClick(int colorCode) {
                textColorIndex = colorPickerColors.indexOf(colorCode);
            }
        });

        View canvasContainer = binding.canvasContainerLayout;
        View textAdditionalFragmentView = binding.textAdditionalFragment.getFragment().getView();

        assert textAdditionalFragmentView != null;
        textAdditionalFragmentView.setVisibility(View.VISIBLE);

        ViewCompat.setWindowInsetsAnimationCallback(canvasContainer, new FragmentHeightAnimator(canvasContainer));
        ViewCompat.setWindowInsetsAnimationCallback(textAdditionalFragmentView, new FragmentHeightAnimator(textAdditionalFragmentView));
    }

    private void saveTypingModeResult(String text, int colorCode) {
        photoEditorSDK.addText(text, colorCode);
        setEditMode(true);
        // photoEditorSDK.clearAllViews();
    }

    protected void setEditMode(boolean isEditMode) {
        if (isEditMode) {
            modeControls.setVisibility(View.INVISIBLE);
            bottomControls.setVisibility(View.INVISIBLE);
            setActionControlsVisibility(View.VISIBLE);
        } else {
            modeControls.setVisibility(View.VISIBLE);
            bottomControls.setVisibility(View.VISIBLE);
            setActionControlsVisibility(View.INVISIBLE);
        }
    }

    private void setupViewListeners() {
        binding.imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int imageWidth = v.getWidth();
                int imageHeight = v.getHeight();

                ViewGroup.LayoutParams layoutParams = binding.drawingView.getLayoutParams();
                layoutParams.width = imageWidth;
                layoutParams.height = imageHeight;
                binding.drawingView.setLayoutParams(layoutParams);
            }
        });
    }

    private void setupWindowInsetsListeners() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View view, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                binding.modeControlsLayout.setPadding(
                        binding.modeControlsLayout.getPaddingLeft(),
                        binding.modeControlsLayout.getPaddingTop() + systemBars.top,
                        binding.modeControlsLayout.getPaddingRight(),
                        binding.modeControlsLayout.getPaddingBottom()
                );

                binding.bottomControlsLayout.setPadding(
                        binding.bottomControlsLayout.getPaddingLeft(),
                        binding.bottomControlsLayout.getPaddingTop(),
                        binding.bottomControlsLayout.getPaddingRight(),
                        systemBars.bottom + 32
                );

                binding.canvasLayout.setPadding(
                        binding.canvasLayout.getPaddingLeft(),
                        binding.canvasLayout.getPaddingTop(),
                        binding.canvasLayout.getPaddingRight(),
                        0
                );

                return insets;
            }
        });
    }
}