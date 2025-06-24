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

public class InsetsWithKeyboardCallbackHelper extends WindowInsetsAnimationCompat.Callback implements OnApplyWindowInsetsListener {
    private boolean deferredInsets = false;
    private WindowInsetsCompat lastWindowInsets = null;
    private View view = null;
    float startBottom;

    public InsetsWithKeyboardCallbackHelper(Window window) {
        super(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE);

        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }


    @NonNull
    @Override
    public WindowInsetsCompat onApplyWindowInsets(@NonNull View view, @NonNull WindowInsetsCompat insets) {
        this.view = view;
        lastWindowInsets = insets;
        // System Bars' Insets
        final Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //  System Bars' and Keyboard's insets combined
        final Insets systemBarsIMEInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime());

        // We use the combined bottom inset of the System Bars and Keyboard to move the view so it doesn't get covered up by the keyboard
        view.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsIMEInsets.bottom);
        return WindowInsetsCompat.CONSUMED;
    }

    @Override
    public void onPrepare(WindowInsetsAnimationCompat animation) {
        if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
            // When the IME is not visible, we defer the WindowInsetsCompat.Type.ime() insets
            deferredInsets = true;
        }
    }

    @NonNull
    @Override
    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
        return insets;
    }

    @Override
    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
        if (deferredInsets && (animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
            // When the IME animation has finished and the IME inset has been deferred, we reset the flag
            deferredInsets = false;
            // We dispatch insets manually because if we let the normal dispatch cycle handle it, this will happen too late and cause a visual flicker
            // So we dispatch the latest WindowInsets to the view
            if (lastWindowInsets != null && view != null) {
                ViewCompat.dispatchApplyWindowInsets(view, lastWindowInsets);
            }
        }
    }
}
