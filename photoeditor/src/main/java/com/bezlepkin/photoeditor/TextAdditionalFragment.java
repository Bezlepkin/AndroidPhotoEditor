package com.bezlepkin.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.adapters.ColorPickerAdapter;
import com.bezlepkin.photoeditor.databinding.TextAdditionalFragmentBinding;

import java.util.List;

public class TextAdditionalFragment extends Fragment {
    private View view;
    private ImageButton applyButton;
    private EditText textInput;
    private TextAdditionalFragmentBinding binding;
    private ColorPickerAdapter colorPickerAdapter;
    private OnApplyClickListener onApplyClickListener;
    private OnColorPickerClickListener onColorPickerClickListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = TextAdditionalFragmentBinding.inflate(inflater, container, false);
        view = binding.textAdditionalLayout;
        binding.textAdditionalLayout.setVisibility(View.INVISIBLE);
        textInput = binding.textInput;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
    }

    public void setOnApplyClickListener(TextAdditionalFragment.OnApplyClickListener onApplyClickListener) {
        this.onApplyClickListener = onApplyClickListener;
    }

    public void setOnColorPickerClickListener(TextAdditionalFragment.OnColorPickerClickListener onColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener;
    }

    public void setup(Context context, Integer color, @NonNull List<Integer> colors) {
        textInput = binding.textInput;
        applyButton = binding.applyButton;
        final RecyclerView сolorPicker = binding.addTextColorPickerRecyclerView;
        colorPickerAdapter = new ColorPickerAdapter(context, colors);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        textInput.setText("");
        textInput.setTextColor(color);
        textInput.setHintTextColor(Color.parseColor("#40FFFFFF"));
        textInput.requestFocus();

        сolorPicker.setLayoutManager(layoutManager);
        сolorPicker.setHasFixedSize(true);
        сolorPicker.setAdapter(colorPickerAdapter);

        initListeners();
        openKeyboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openKeyboard() {
        textInput.post(new Runnable() {
            @Override
            public void run() {
                textInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(textInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View rootView = getView();
        if (rootView != null) {
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
    }

    private void initListeners() {
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textAdditionalLayout.setVisibility(View.INVISIBLE);
                hideKeyboard();
                if (onApplyClickListener != null) {
                    onApplyClickListener.onClick(textInput.getText().toString(), textInput.getCurrentTextColor());
                }
            }
        });

        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int color) {
                if (onColorPickerClickListener != null) {
                    onColorPickerClickListener.onClick(color);
                }
                textInput.setTextColor(color);
            }
        });
    }

    public interface OnColorPickerClickListener {
        void onClick(int colorCode);
    }

    public interface OnApplyClickListener {
        void onClick(String text, int color);
    }
}
