package com.adbert.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.adbert.util.SDKUtil;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.Colors;
import com.adbert.util.list.CustomViewListener;

public class CPMBannerView extends RelativeLayout {

    Context context;
    boolean screenPortrait;
    CustomViewListener listener;
    int btnh;
    CommonData videoInfo;
    float pWidth, pHeight;
    RelativeLayout fmbgCPM;
    ImageView bannerCPM;
    Bitmap bitmap;

    public CPMBannerView(Context context, boolean screenPortrait, CommonData videoInfo,
                         float pWidth, float pHeight, int btnh, CustomViewListener listener) {
        super(context);
        SDKUtil.logTestMsg("CPMBannerView pWidth = " + pWidth + ",pHeight = " + pHeight);
        this.context = context;
        this.screenPortrait = screenPortrait;
        this.listener = listener;
        this.videoInfo = videoInfo;
        this.btnh = btnh;
        this.pWidth = pWidth;
        this.pHeight = pHeight;
        setCPMBanner();
    }

    private void setCPMBanner() {
        fmbgCPM = this;
        fmbgCPM.setBackgroundColor(Colors.cpmBg.parseColor());
        if (SDKUtil.isGIF(videoInfo.cpmBannerImg) || videoInfo.adServing) {
            setWeb();
        } else {
            setImageView();
        }
        //set delete
        CustomCloseButton delete = new CustomCloseButton(getContext(), btnh);
        fmbgCPM.addView(delete);
        int size = (int) (btnh * SDKUtil.closeRangeScale);
        delete.getLayoutParams().width = size;
        delete.getLayoutParams().height = size;
//        delete.setBackgroundColor(Color.RED);
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.finish();
            }
        });


        fmbgCPM.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
        listener.setLogo(fmbgCPM, true);
    }

    private void setImageView() {
        bannerCPM = new ImageView(context);
        bannerCPM.setScaleType(ScaleType.FIT_XY);
        if (screenPortrait) {
            fmbgCPM.addView(bannerCPM, new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            bannerCPM.getLayoutParams().width = (int) (pWidth * 0.9);
            bannerCPM.getLayoutParams().height = (int) ((pWidth * 0.9 / 320) * 480);
        } else {
            fmbgCPM.addView(bannerCPM, new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            bannerCPM.getLayoutParams().width = (int) ((pHeight * 0.9 / 320) * 480);
            bannerCPM.getLayoutParams().height = (int) (pHeight * 0.9);
        }
        ((LayoutParams) bannerCPM.getLayoutParams())
                .addRule(RelativeLayout.CENTER_IN_PARENT);
        String savePath = SDKUtil.getFileNameFromUrl(context, videoInfo.cpmBannerImg);
        bitmap = BitmapFactory.decodeFile(savePath);
        bannerCPM.setImageBitmap(bitmap);
        bannerCPM.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < 5; i++) {
                    if (videoInfo.endingCard[i]) {
                        listener.endingCardAction(i);
                        break;
                    }
                }
            }
        });
    }

    private void setWeb() {
        RelativeLayout ll = new RelativeLayout(context);
        GIFView gifView = new GIFView(context, listener, videoInfo.type);
        gifView.load(videoInfo.cpmBannerImg, videoInfo);
        if (screenPortrait) {
            fmbgCPM.addView(ll, new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            ll.getLayoutParams().width = (int) (pWidth * 0.9);
            ll.getLayoutParams().height = (int) ((pWidth * 0.9 / 320) * 480);
        } else {
            fmbgCPM.addView(ll, new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            ll.getLayoutParams().height = (int) (pHeight * 0.9);
            ll.getLayoutParams().width = (int) ((pHeight * 0.9 / 320) * 480);
        }
        ((LayoutParams) ll.getLayoutParams())
                .addRule(RelativeLayout.CENTER_IN_PARENT);
        ll.addView(gifView);
        RelativeLayout cover = new RelativeLayout(context);
        ll.addView(cover, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        cover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < 5; i++) {
                    if (videoInfo.endingCard[i]) {
                        listener.endingCardAction(i);
                        break;
                    }
                }
            }
        });
    }

    public void destroy() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.isRecycled();
            bitmap = null;
        }
    }
}
