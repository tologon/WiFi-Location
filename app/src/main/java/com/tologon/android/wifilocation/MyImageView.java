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
    private Paint wiFiPaint, userPaint;
    private final int TRANSPARENCY = 100;
    private final int WIFI_RADIUS = 50, USER_RADIUS = 20;
    private int backX = 118, backY = 569;
    private int centerX = 144, centerY = 1304;
    private int frontX = 672, frontY = 1424;
    private int userX, userY;

    public MyImageView(Context context) {
        super(context);
        setPaints();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPaints();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPaints();
        setMapImage();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPaints();
        setMapImage();
    }

    private void setMapImage() {
        setImageResource(R.drawable.dobbs_hall_1);
    }

    private void setPaints() {
        wiFiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wiFiPaint.setColor(Color.GREEN);
        wiFiPaint.setAlpha(TRANSPARENCY);

        userPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPaint.setColor(Color.CYAN);
        userPaint.setAlpha(TRANSPARENCY);
    }

    public void moveUser(int x, int y) {
        userX = x;
        userY = y;
    }

    public int getUserX() {
        return userX;
    }

    public int getUserY() {
        return userY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // WiFi Access Point in the back of CS department
        drawWIFIPoint(backX, backY, canvas);
        // WiFi Access Point in the middle of CS department
        drawWIFIPoint(centerX, centerY, canvas);
        // WiFi Access Point in the front of CS department
        drawWIFIPoint(frontX, frontY, canvas);
        // User position
        drawUser(userX, userY, canvas);
    }

    private void drawWIFIPoint(int x, int y, Canvas canvas) {
        canvas.drawCircle(x, y, WIFI_RADIUS, wiFiPaint);
    }

    private void drawUser(int x, int y, Canvas canvas) {
        canvas.drawCircle(x, y, USER_RADIUS, userPaint);
    }
}
