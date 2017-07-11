/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.android.devdemo.sigcapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SignatureArea extends SurfaceView implements SurfaceHolder.Callback {

    public SignatureArea(Context context) {
        this(context, null);
    }

    public SignatureArea(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setFocusable(true); // make sure we get key events

        black = new Paint();
        black.setColor(Color.BLACK);

        setWillNotDraw(false);

    }

    private float lastPointX;
    private float lastPointY;
    private Paint black;
    private Bitmap signatureAreaContent = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastPointX = event.getX();
            lastPointY = event.getY();

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Canvas c = new Canvas(signatureAreaContent);
            c.drawLine(lastPointX, lastPointY, event.getX(), event.getY(), black);
            lastPointX = event.getX();
            lastPointY = event.getY();
            postInvalidate();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Canvas c = new Canvas(signatureAreaContent);
            c.drawLine(lastPointX, lastPointY, event.getX(), event.getY(), black);
            postInvalidate();
        }
        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Paint lightGray = new Paint();
        lightGray.setColor(Color.LTGRAY);
        signatureAreaContent = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(signatureAreaContent);
        c.drawRect(0, 0, width, height, lightGray);
        invalidate();
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (signatureAreaContent != null) {
            canvas.drawBitmap(signatureAreaContent, 0, 0, null);
        }
        super.onDraw(canvas);
    }

    public Bitmap getBitmap() {
        return signatureAreaContent;
    }

}
