package com.adbert.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adbert.util.enums.Colors;
import com.adbert.util.enums.Drawables;

/**
 * Created by chihhan on 2017/7/6.
 */

public class ToolBarView extends LinearLayout {

    public interface ItemClickListener {
        void onClick(int position);
    }

    public ToolBarView(Context context, int orientation) {
        super(context);
        setOrientation(orientation);
        setBackgroundColor(Colors.endingCardBg.parseColor());
        if (orientation == LinearLayout.VERTICAL) {
            setGravity(Gravity.CENTER_HORIZONTAL);
        }else{
            setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    String[] tags = new String[]{"download", "web", "phone", "fb", "line"};

    public void setItem(boolean[] showItem, int btnh, final ItemClickListener listener) {
        for (int i = 0; i < tags.length; i++) {
            RelativeLayout ll = new RelativeLayout(getContext());
            addView(ll);
            ((LayoutParams) ll.getLayoutParams()).weight = 1;
            ImageView img = new ImageView(getContext());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(img, lp);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            img.setImageDrawable(Drawables.download.createDrawable(getContext(), i));
            img.getLayoutParams().width = btnh;
            img.getLayoutParams().height = btnh;
            if (showItem[i]) {
                ll.setVisibility(View.VISIBLE);
            } else {
                ll.setVisibility(View.GONE);
            }
            final int index = i;
            img.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onClick(index);
                }
            });
        }
    }

}
