package com.adbert.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adbert.util.SDKUtil;
import com.adbert.util.enums.Drawables;

/**
 * Created by chihhan on 2017/8/17.
 */

public class CustomCloseButton extends RelativeLayout {

    private int btnh;
    private boolean setAlpha = false;

    public CustomCloseButton(Context context, int btnh) {
        super(context);
        this.btnh = (int) (btnh * SDKUtil.closeScale);
        init();
    }

//    public CustomCloseButton(Context context, int btnh, boolean setAlpha) {
//        super(context);
//        this.btnh = (int) (btnh * SDKUtil.closeScale);
//        this.setAlpha = setAlpha;
//        init();
//    }

    public CustomCloseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCloseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomCloseButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ImageView delete = new ImageView(getContext());
        this.addView(delete);
        delete.setImageDrawable(Drawables.delete.createDrawable(getContext()));
        ((LayoutParams) delete.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
        delete.getLayoutParams().width = btnh;
        delete.getLayoutParams().height = btnh;
        if (setAlpha) {
            Drawable d = delete.getDrawable();
            d.setAlpha(180);
            delete.setImageDrawable(d);
        }
    }
}
