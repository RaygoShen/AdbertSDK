package com.adbert;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * No longer need this to handle click event.
 *
 * @deprecated use AdbertNativeAD.registerView(view) instead.
 */
@Deprecated
public final class AdbertNativeADView extends RelativeLayout implements View.OnClickListener {

    private AdbertNativeAD nativeAD = null;

    @Override
    public void addView(View child) {
        if (child != null) {
            child.setClickable(false);
        }
        super.addView(child);
        if (cover != null)
            cover.bringToFront();
    }

    View cover;

    private void init() {
        cover = new View(getContext());
        cover.setContentDescription("cover");
        this.addView(cover);
        setView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (cover != null) {
            int height = Math.abs(top - bottom);
            int width = Math.abs(right - left);
            cover.layout(0, 0, width, height);
        }
    }

    public void setAd(final AdbertNativeAD nativeAD) {
        this.nativeAD = nativeAD;
        setView();
    }

    private void setView() {
        this.setClickable(true);
        cover.setClickable(false);
        cover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isValidStatus()) {
                    nativeAD.click();
                }
            }
        });
    }

    public AdbertNativeADView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    public AdbertNativeADView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public AdbertNativeADView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    private boolean isValidStatus() {
        if (getChildCount() != 0) {
            if (nativeAD != null && nativeAD.isReady()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (isValidStatus()) {
            nativeAD.click();
        }
    }
}