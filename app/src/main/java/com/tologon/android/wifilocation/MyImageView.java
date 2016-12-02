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
    public static final int FRONT = 0;
    public static final int CENTER = 1;
    public static final int BACK = 2;
    public static final int DEFAULT_RADIUS = 50;
    private int FRONT_AP_RADIUS = DEFAULT_RADIUS;
    private int CENTER_AP_RADIUS = DEFAULT_RADIUS;
    private int BACK_AP_RADIUS = DEFAULT_RADIUS;

    private int USER_RADIUS = 20;
    private int backX = 118, backY = 510;
    private int centerX = 144, centerY = 1195;
    private int frontX = 672, frontY = 1285;
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

    private void setMapImage() {
        setImageResource(R.drawable.dobbs_hall_1);
    }

    private void setPaints() {
        wiFiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wiFiPaint.setColor(Color.GREEN);
        wiFiPaint.setAlpha(TRANSPARENCY);

        userPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPaint.setColor(Color.RED);
        userPaint.setAlpha(TRANSPARENCY);
    }

    public int getBackX() {
        return backX;
    }

    public int getBackY() {
        return backY;
    }

    public int getBackRadius() {
        return BACK_AP_RADIUS;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getCenterRadius() {
        return CENTER_AP_RADIUS;
    }

    public int getFrontX() {
        return frontX;
    }

    public int getFrontY() {
        return frontY;
    }

    public int getFrontRadius() {
        return FRONT_AP_RADIUS;
    }

    public void setUser(int x, int y) {
        userX = x;
        userY = y;
    }

    public int getUserX() {
        return userX;
    }

    public int getUserY() {
        return userY;
    }

    public void setWifiRadius(int wifiAP, int newRadius) {
        switch (wifiAP) {
            case FRONT:     FRONT_AP_RADIUS = newRadius;
                            break;
            case CENTER:    CENTER_AP_RADIUS = newRadius;
                            break;
            case BACK:      BACK_AP_RADIUS = newRadius;
                            break;
        }
    }

    public int getWifiRadius(int wifiAP) {
        switch (wifiAP) {
            case FRONT:     return FRONT_AP_RADIUS;
            case CENTER:    return CENTER_AP_RADIUS;
            case BACK:      return BACK_AP_RADIUS;
            default:        return 1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // WiFi Access Point in the back of CS department
        drawWIFIPoint(backX, backY, BACK_AP_RADIUS, canvas);
        // WiFi Access Point in the middle of CS department
        drawWIFIPoint(centerX, centerY, CENTER_AP_RADIUS, canvas);
        // WiFi Access Point in the front of CS department
        drawWIFIPoint(frontX, frontY, FRONT_AP_RADIUS, canvas);
        // User position
        drawUser(userX, userY, USER_RADIUS, canvas);
    }

    private void drawWIFIPoint(int x, int y, int radius, Canvas canvas) {
        canvas.drawCircle(x, y, radius, wiFiPaint);
    }

    private void drawUser(int x, int y, int radius, Canvas canvas) {
        canvas.drawCircle(x, y, radius, userPaint);
    }
}
