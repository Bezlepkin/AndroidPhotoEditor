package com.bezlepkin.photoeditor.helpers;


import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class InsetsWithKeyboardAnimationCallbackHelper extends WindowInsetsAnimationCompat.Callback {
    private final View view;

    public InsetsWithKeyboardAnimationCallbackHelper(View view) {
        super(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP);
        this.view = view;
    }

    @NonNull
    @Override
    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
        Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
        Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

        Insets diff = Insets.subtract(imeInsets, systemInsets);
        diff = Insets.max(diff, Insets.NONE);
        Log.d("setTranslationY_TOP", String.valueOf(diff.top) + " " + String.valueOf(diff.bottom));
        // view.setTranslationX((diff.left - diff.right));
        view.setTranslationY((diff.top - diff.bottom));

        return insets;
    }

    @Override
    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
        Log.d("setTranslationY_TOP", "FINISHED");
        // We reset the translation values after the animation has finished
        //view.setTranslationX(0f);
        //view.setTranslationY(0f);
    }
}
