package com.bezlepkin.photoeditor.animator;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsAnimationCompat;

import java.util.List;

public class KeyboardOffsetAnimator extends WindowInsetsAnimationCompat.Callback {
    private final View view;
    private boolean isImeOpening;

    public KeyboardOffsetAnimator(View view) {
        super(DISPATCH_MODE_STOP);
        this.view = view;
    }

    @NonNull
    @Override
    public WindowInsetsAnimationCompat.BoundsCompat onStart(
            @NonNull WindowInsetsAnimationCompat animation,
            @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds
    ) {
        int startPadding = view.getPaddingBottom();
        int endPadding = bounds.getUpperBound().bottom;
        isImeOpening = endPadding > startPadding;
        return super.onStart(animation, bounds);
    }

    @NonNull
    @Override
    public WindowInsetsCompat onProgress(
            @NonNull WindowInsetsCompat insets,
            @NonNull List<WindowInsetsAnimationCompat> runningAnimations
    ) {
        int imePadding = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
        int systemBarsPadding = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
        view.setPadding(
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                isImeOpening ? imePadding : imePadding + systemBarsPadding
        );
        return insets;
    }
}
