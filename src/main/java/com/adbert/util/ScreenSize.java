package com.adbert.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenSize {

    int width, height;
    Context context;

    public ScreenSize(Context context) {
        this.context = context;
        try {
            DisplayMetrics dm = new DisplayMetrics();
            Activity activty = (Activity) context;
            activty.getWindowManager().getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        } catch (Exception e) {
            SDKUtil.logException(e);

            try {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            } catch (Exception e2) {
                SDKUtil.logException(e2);
            }
        }
    }

    public int getScreenWidth() {
        return width;
    }

    public int getScreenHeight() {
        return height;
    }

    public String getScreenSize() {
        return width + "X" + height;
    }

    public void log() {
//        Log.d("ScreenSize", width + "," + height);
    }

    public int getPercentOfWidth(double percent) {
        return (int) ((float) width * percent);
    }

    public int getPercentOfHeight(double percent) {
        return (int) ((float) height * percent);
    }

    public int getHeight(int width, int scaleW, int scaleH) {
        return (int) (((float) width / (float) scaleW) * (float) scaleH);
    }

    public int getHeight(int scaleW, int scaleH) {
        return (int) (((float) width / (float) scaleW) * (float) scaleH);
    }

    public int getWidth(float height, int scaleW, int scaleH) {
        return (int) (((float) height / (float) scaleH) * (float) scaleW);
    }

    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public int px2dip(float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public int getStatusHeight() {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            } catch (IllegalAccessException e) {
//                e.printStackTrace();
            } catch (InstantiationException e) {
//                e.printStackTrace();
            } catch (NumberFormatException e) {
//                e.printStackTrace();
            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
            } catch (SecurityException e) {
//                e.printStackTrace();
            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
            }
        }
        return statusHeight;
    }
}
