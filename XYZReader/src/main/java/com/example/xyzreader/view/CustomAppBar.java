package com.example.xyzreader.view;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

/**
 * Aspect Ratio View
 * Make AppBar Height exactly 2/3 of the width
 */

public class CustomAppBar extends AppBarLayout {


    public CustomAppBar(Context context) {
        super(context);
    }

    public CustomAppBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int threetwoHeight = MeasureSpec.getSize(widthMeasureSpec) * 1/3;
        int threetwoHeightSpec = MeasureSpec.makeMeasureSpec(threetwoHeight,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, threetwoHeightSpec);

    }
}
