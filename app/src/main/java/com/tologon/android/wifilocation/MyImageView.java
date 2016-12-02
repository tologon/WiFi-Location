package com.tologon.android.wifilocation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by teshimkanov on 12/1/16.
 */

public class MyImageView extends ImageView {
    private Paint paint;
    private final int TRANSPARENCY = 100;
    private final int RADIUS = 50;
    private final int MAP_X = 56;
    private final int MAP_Y = 336;

    public MyImageView(Context context) {
        super(context);
        setPaint();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPaint();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPaint();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPaint();
        setMapImage();
    }

    private void setMapImage() {
        setImageResource(R.drawable.dobbs_hall_1);
    }

    private void setPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setAlpha(TRANSPARENCY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // WiFi Access Point in the back of CS department
        drawWIFIPoint(118, 569, canvas);
        // WiFi Access Point in the back of CS department
        drawWIFIPoint(144, 1304, canvas);
        // WiFi Access Point in the back of CS department
        drawWIFIPoint(672, 1424, canvas);
    }

    private void drawWIFIPoint(int x, int y, Canvas canvas) {
        canvas.drawCircle(x, y, RADIUS, paint);
    }
}
