package com.bezlepkin.photoeditor.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bezlepkin.photoeditor.R;

import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {
    // private Context context;
    private final LayoutInflater inflater;
    private final List<Integer> colorPickerColors;
    private int activeColorIndex = 0;
    private OnColorPickerClickListener onColorPickerClickListener;

    public ColorPickerAdapter(@NonNull Context context, @NonNull List<Integer> colorPickerColors) {
        // this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.colorPickerColors = colorPickerColors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.color_picker_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == activeColorIndex) {
            buildActiveColorPickerView(holder.colorPickerView, colorPickerColors.get(position));
        } else {
            buildInactiveColorPickerView(holder.colorPickerView, colorPickerColors.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return colorPickerColors.size();
    }

    private void buildActiveColorPickerView(View view, int colorCode) {
        view.setVisibility(View.VISIBLE);
        // bigger
        ShapeDrawable middleCircle = new ShapeDrawable(new OvalShape());
        middleCircle.setPadding(6, 6, 6, 6);
        middleCircle.getPaint().setColor(colorCode);

        ShapeDrawable biggerCircle = new ShapeDrawable(new OvalShape());
        biggerCircle.setPadding(10, 10, 10, 10);
        biggerCircle.getPaint().setColor(Color.BLACK);

        ShapeDrawable smallerCircle = new ShapeDrawable(new OvalShape());
        smallerCircle.getPaint().setColor(colorCode);

        Drawable[] drawables = {middleCircle, biggerCircle, smallerCircle};
        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        view.setBackground(layerDrawable);
    }

    private void buildInactiveColorPickerView(View view, int colorCode) {
        view.setVisibility(View.VISIBLE);

        ShapeDrawable biggerCircle = new ShapeDrawable(new OvalShape());
        biggerCircle.setIntrinsicHeight(20);
        biggerCircle.setIntrinsicWidth(20);
        biggerCircle.setBounds(new Rect(0, 0, 20, 20));
        biggerCircle.getPaint().setColor(colorCode);

        ShapeDrawable smallerCircle = new ShapeDrawable(new OvalShape());
        smallerCircle.setIntrinsicHeight(2);
        smallerCircle.setIntrinsicWidth(2);
        smallerCircle.setBounds(new Rect(0, 0, 2, 2));
        smallerCircle.getPaint().setColor(Color.WHITE);
        smallerCircle.setPadding(4, 4, 4, 4);
        Drawable[] drawables = {smallerCircle, biggerCircle};

        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        view.setBackground(layerDrawable);
    }

    public void setActiveColorIndex(int index) {
        activeColorIndex = index;
    }

    public void setOnColorPickerClickListener(OnColorPickerClickListener onColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        View colorPickerView;

        public ViewHolder(View itemView) {
            super(itemView);
            colorPickerView = itemView.findViewById(R.id.color_picker_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View v) {
                    if (onColorPickerClickListener != null) {
                        setActiveColorIndex(getAdapterPosition());
                        onColorPickerClickListener.onColorPickerClickListener(colorPickerColors.get(getAdapterPosition()));
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public interface OnColorPickerClickListener {
        void onColorPickerClickListener(int colorCode);
    }
}