package com.bezlepkin.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.adapters.ColorPickerAdapter;

import java.util.List;

public class TextAdditionalPopupWindow extends PopupWindow {
    private final View view;
    private final ImageButton applyButton;
    private final EditText textInput;
    private final ColorPickerAdapter colorPickerAdapter;
    private OnApplyClickListener onApplyClickListener;
    private OnColorPickerClickListener onColorPickerClickListener;

    public void setOnApplyClickListener(OnApplyClickListener onApplyClickListener) {
        this.onApplyClickListener = onApplyClickListener;
    }

    public void setOnColorPickerClickListener(OnColorPickerClickListener onColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener;
    }

    @SuppressLint("InflateParams")
    public TextAdditionalPopupWindow(Context context, Integer color, @NonNull List<Integer> colors) {
        super(context, null);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.text_additional_popup, null);

        textInput = view.findViewById(R.id.text_input);
        applyButton = view.findViewById(R.id.apply_button);
        final RecyclerView addTextColorPickerRecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_view);
        colorPickerAdapter = new ColorPickerAdapter(context, colors);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        textInput.setTextColor(color);
        textInput.setHintTextColor(Color.parseColor("#40FFFFFF"));
        textInput.requestFocus();

        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);

        initListeners();
    }

    public void show() {
        setContentView(view);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(null);
        showAtLocation(view, Gravity.TOP, 0, 0);
    }

    private void initListeners() {
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
