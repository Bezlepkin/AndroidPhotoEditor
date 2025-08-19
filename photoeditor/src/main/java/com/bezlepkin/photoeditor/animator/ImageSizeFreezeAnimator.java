package com.bezlepkin.photoeditor.animator;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class ImageSizeFreezeAnimator extends WindowInsetsAnimationCompat.Callback {
    private final View viewToFreeze;
    private int initialWidth;
    private int initialHeight;

    public ImageSizeFreezeAnimator(@NonNull View viewToFreeze) {
        super(DISPATCH_MODE_CONTINUE_ON_SUBTREE);
        this.viewToFreeze = viewToFreeze;
    }

    @Override
    public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
        initialWidth = viewToFreeze.getWidth();
        initialHeight = viewToFreeze.getHeight();
        Log.d("ImageSizeFreezeAnimator", "onPrepare initialImageViewWidth: " + initialWidth);
        Log.d("ImageSizeFreezeAnimator", "onPrepare initialImageViewHeight: " + initialHeight);
    }


    @NonNull
    @Override
    public WindowInsetsAnimationCompat.BoundsCompat onStart(@NonNull WindowInsetsAnimationCompat animation, @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
        Log.d("ImageSizeFreezeAnimator", "onStart initialImageViewWidth: " + initialWidth);
        Log.d("ImageSizeFreezeAnimator", "onStart initialImageViewHeight: " + initialHeight);
        ViewGroup.LayoutParams params = viewToFreeze.getLayoutParams();
        params.width = initialWidth;
        params.height = initialHeight;
        viewToFreeze.setLayoutParams(params);
        return bounds;
    }

    @NonNull
    @Override
    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
        return insets;
    }


    @Override
    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
        ViewGroup.LayoutParams params = viewToFreeze.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewToFreeze.setLayoutParams(params);
    }
}
