package com.bezlepkin.photoeditor;

import android.content.Context;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class Constant {
    public static ArrayList<Integer> getDefaultColors(Context context) {
        // the Material colors are used here https://materialui.co/colors
        return new ArrayList<Integer>(Arrays.asList(
                ContextCompat.getColor(context, R.color.white),
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.red),
                ContextCompat.getColor(context, R.color.green),
                ContextCompat.getColor(context, R.color.blue),
                ContextCompat.getColor(context, R.color.yellow),
                ContextCompat.getColor(context, R.color.pink),
                ContextCompat.getColor(context, R.color.orange),
                ContextCompat.getColor(context, R.color.brown)
        ));
    }
}
