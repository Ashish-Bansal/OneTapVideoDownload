package com.phantom.onetapvideodownload.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class SquareView extends View {
    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? width : height;
        setMeasuredDimension(size, size);
    }
}
