/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

public class PrinterBackgroundView extends View {
    private Paint paint;

    public PrinterBackgroundView(Context context) {
        super(context);
    }

    public PrinterBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrinterBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int maxSize = Math.max(getHeight(), getWidth());
        RadialGradient gradient = new RadialGradient(
                getWidth() / 2,
                getHeight() / 2,
                maxSize,
                new int[] { getResources().getColor(R.color.printer_circle_gradient_center), getResources().getColor(R.color.printer_circle_gradient_edge) },
                new float[] { 0, 1 },
                android.graphics.Shader.TileMode.CLAMP);

        paint.setDither(true);
        paint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
    }
}
