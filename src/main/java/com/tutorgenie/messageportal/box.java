package com.tutorgenie.messageportal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class box extends View
{
    private Paint paint = new Paint();
    box(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        //center
        int x0 = getWidth()/2;
        int y0 = getHeight()/2;
        int dx = getHeight()/3;
        int dy = getHeight()/3;
        //draw guide box
        canvas.drawRect(x0-dx, y0-dy, x0+dx, y0+dy, paint);
    }
}