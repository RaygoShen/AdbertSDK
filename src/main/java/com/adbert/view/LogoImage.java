package com.adbert.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.adbert.util.SDKUtil;
import com.adbert.util.enums.Drawables;

public class LogoImage extends ImageView {

	int w, h;

	public LogoImage(Context context, int w) {
		super(context);
		this.w = w;
		this.h = SDKUtil.countUIH(w, 50, 46);
		Drawable d = Drawables.logo.createDrawable(getContext());
		d.setAlpha(75);
		setImageDrawable(d);
		setScaleType(ScaleType.FIT_END);
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (visibility == View.VISIBLE)
			bringToFront();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = w;
		final int height = h;
		super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
}
