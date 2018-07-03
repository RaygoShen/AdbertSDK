package com.adbert.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class BannerSize {

    private Context context;
    private int pWidth, pHeight;
    private boolean landMode, itemNotFill;
    private int itemW, itemH;
    private int scaleX = 320, scaleY = 50;

    public void setScale(int scaleX, int scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public BannerSize(Context context) {
        this.context = context;
        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            pWidth = dm.widthPixels;
            pHeight = dm.heightPixels;
        } catch (Exception e) {
            SDKUtil.logException(e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                try {
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    pWidth = size.x;
                    pHeight = size.y;
                } catch (Exception e2) {
                    SDKUtil.logException(e2);
                }
            }
        }
    }

    public int getScreenWidth() {
        return pWidth;
    }

    public int getScreenHeight() {
        return pHeight;
    }

    public int getAdbertWidth() {
        if (bannerWidth > 0) {
            return bannerWidth;
        } else {
            if (onlyOnLandscape())
                return getScreenHeight();
            else
                return getScreenWidth();
        }
    }

    public int getAdbertHeight() {
        if (itemNotFill) {
            return itemH;
        } else {
            return countUIH(getAdbertWidth(), scaleX, scaleY);
        }

    }

    private int countUIH(float targetW, int oriW, int oriH) {
        return (int) ((targetW / oriW) * oriH);
    }

    public void setItemSize(int itemH) {
        itemNotFill = true;
        this.itemW = (int) (((float) itemH / (float) scaleY) * (float) scaleX);
        this.itemH = itemH;
    }

    public int getItemWidth() {
        if (itemNotFill)
            return itemW;
        else
            return getAdbertWidth();
    }

    public int getItemHeight() {
        if (itemNotFill)
            return itemH;
        else
            return getAdbertHeight();
    }

    public boolean isScreenPortrait() {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    int bannerWidth = 0;

    public void setBannerWidth(int bannerWidth) {
        this.bannerWidth = bannerWidth;
    }

    private boolean onlyOnLandscape() {
        if (landMode && !isScreenPortrait()) {
            return true;
        } else
            return false;
    }

    public void setLandMode(boolean landMode) {
        this.landMode = landMode;
    }

    public boolean isLandMode() {
        return landMode;
    }
}
