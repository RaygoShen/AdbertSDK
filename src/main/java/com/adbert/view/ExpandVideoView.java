package com.adbert.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adbert.util.SDKUtil;
import com.adbert.util.ScreenSize;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.Colors;
import com.adbert.util.enums.Drawables;
import com.adbert.util.list.CustomViewListener;

import java.io.File;

public class ExpandVideoView extends RelativeLayout {

    private Context context;
    private CommonData videoInfo;
    private int btnh;
    private CustomViewListener listener;
    private float adHeight, adWidth;
    private int defaultSeekTo = 0;
    private TextView reciprocal;
    private ProgressBar loadingProgress;
    private ImageView volume;
    private String reciprocalStr = "";
    private ToolBarView toolBarView;
    private FrameLayout preview;
    private Handler endingCardDisappear = new Handler();
    private boolean top = true;
    private boolean fullScreen = true;
    private int oribtnh;
    private Bitmap bmp;

    public ExpandVideoView(Context context, CommonData data, int btnh, CustomViewListener viewListener) {
        super(context);
        this.context = context;
        this.videoInfo = data;
        this.btnh = (int) (btnh * 0.8);
        this.oribtnh = btnh;
        ScreenSize screenSize = new ScreenSize(context);
        adWidth = screenSize.getScreenWidth();
        adHeight = screenSize.getScreenHeight();
        this.listener = viewListener;
        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.closeAdView();
            }
        });
    }

    public void setSize(int videoW, int videoH) {
        adWidth = videoW;
        adHeight = videoH;
    }

    public int getSeekTo() {
        if (video != null) {
            return video.getCurrentPosition();
        } else {
            return defaultSeekTo;
        }
    }

    private boolean isScreenPortrait() {
        return SDKUtil.isPortrait(context);
    }

    public void showCPMVideo(boolean hideCI) {
        setViewType(true, isScreenPortrait(), false);
        initView();
    }

    public void showCPV(int seekTo, boolean top, boolean fullScreen) {
        setViewType(false, SDKUtil.isPortrait(context), false);
        this.defaultSeekTo = seekTo;
        this.top = top;
        this.fullScreen = fullScreen;
        initView();
    }

    public void showNativeVideo() {
        setViewType(false, SDKUtil.isPortrait(context), true);
        this.top = false;
        this.fullScreen = false;
        initView();
    }

    private enum ViewType {
        CPMVideo_V, CPMVideo_H, CPV_V, CPV_H, NativeVideo
    }

    private ViewType viewType;

    private ViewType getViewType() {
        return viewType;
    }

    private void setViewType(boolean isCPM, boolean screenPortrait, boolean isNative) {
        if (isCPM && screenPortrait) {
            viewType = ViewType.CPMVideo_V;
        } else if (isCPM && !screenPortrait) {
            viewType = ViewType.CPMVideo_H;
        } else if (isNative) {
            viewType = ViewType.NativeVideo;
        } else if (!isCPM && !screenPortrait) {
            viewType = ViewType.CPV_H;
        } else {
            viewType = ViewType.CPV_V;
        }
    }

    private void initView() {
        // set layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        this.addView(layout, getLP(true, false));
        // set adll
        RelativeLayout adll = new RelativeLayout(context);
        layout.addView(adll, getLP(true, false));
        adll.getLayoutParams().height = getVideoHeight();
        adll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showEndingCard();
            }
        });
        // set bg color area
        if (isCPM()) {
            for (int i = 0; i < 2; i++) {
                View view = new View(context);
                this.addView(view);
                view.getLayoutParams().width = (int) adWidth;
                view.getLayoutParams().height = getBgColorAreaHeight();
                view.setBackgroundColor(Colors.cpmBg.parseColor());
                if (i == 1) {
                    ((LayoutParams) view.getLayoutParams())
                            .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
            }
        }
        setVideoView(adll);
        setLoadingBar(layout);
        if (getViewType() == ViewType.CPMVideo_V) {
            setCPMVideoImage(layout);
        }
        // set delete
        if (isCPM() || getViewType() == ViewType.CPV_H) {
            CustomCloseButton delete = new CustomCloseButton(getContext(), oribtnh);
            this.addView(delete);
            ((LayoutParams) delete.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            int size = (int) (btnh * SDKUtil.closeRangeScale);
            delete.getLayoutParams().width = size;
            delete.getLayoutParams().height = size;
//            delete.setBackgroundColor(Color.RED);
            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.closeVideo();
                }
            });
        }

        if (isCPM() || getViewType() == ViewType.NativeVideo) {
            ((LayoutParams) layout.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
        } else if (!top) {
            ((LayoutParams) layout.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }

    private void setLoadingBar(ViewGroup layout) {
        loadingProgress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        layout.addView(loadingProgress, getLP(true, false));
        loadingProgress.getLayoutParams().height = 3;
        loadingProgress.getProgressDrawable().setColorFilter(Color.RED, Mode.MULTIPLY);
        loadingProgress.setMax(100);
        loadingProgress.setProgress(0);
    }

    private StretchVideoView video;

    private void setVideoView(ViewGroup adll) {
        // add videoll
        final FrameLayout videoll2 = new FrameLayout(context);
        adll.addView(videoll2);
        // add volume
        volume = new ImageView(context);
        CIPosition cip = getCIPostion();
        if (cip == CIPosition.under_volume) {
            LinearLayout btnll = new LinearLayout(context);
            btnll.setOrientation(LinearLayout.VERTICAL);
            btnll.addView(volume, btnh, btnh);
            listener.setLogo(btnll, false);
            adll.addView(btnll);
        } else if (cip == CIPosition.leftAndBottom) {
            adll.addView(volume, btnh, btnh);
            listener.setLogo(this, true);
        } else if (cip == CIPosition.leftAndTop) {
            LinearLayout btnll = new LinearLayout(context);
            btnll.setOrientation(LinearLayout.VERTICAL);
            ImageView img = new ImageView(context);
            btnll.addView(img, btnh / 2, btnh / 2);
            listener.setLogo(btnll, false);
            this.addView(btnll);
            adll.addView(volume, btnh, btnh);
        }
        volume.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                videoInfo.volumeOpen = !videoInfo.volumeOpen;
                setVolumeStatus();
            }
        });
        // set reciprocal
        reciprocal = new TextView(context);
        reciprocal.setShadowLayer(1, 1, 1, Color.GRAY);
        reciprocal.setTextColor(Color.WHITE);
        adll.addView(reciprocal);
        ((LayoutParams) reciprocal.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //
        if (!videoInfo.absolute) {
            setEndingCard(adll);
        }
        // set video
        video = new StretchVideoView(context, (int) adWidth, getVideoHeight());
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoll2.addView(video, lp);
        video.setListener(videoListener);
        // set preview layout
        preview = new FrameLayout(context);
        preview.setBackgroundColor(Color.BLACK);
        videoll2.addView(preview, getLP(true, false));
        preview.getLayoutParams().height = getVideoHeight();
        // set source
        String path = videoInfo.mediaSource;
        if (new File(SDKUtil.getFileNameFromUrl(context, path)).exists()) {
            path = SDKUtil.getFileNameFromUrl(context, path);
        }
        video.setUrl(path);
    }

    private void setVolumeStatus() {
        if (!videoInfo.volumeOpen) {
            volume.setImageDrawable(Drawables.mute.createDrawable(getContext()));
            if (video != null) {
                video.volumeClose();
            }
        } else {
            volume.setImageDrawable(Drawables.sound.createDrawable(getContext()));
            if (video != null) {
                video.volumeOpen();
            }
        }
    }

    private void setCPMVideoImage(ViewGroup layout) {
        // set layout
        RelativeLayout imgll = new RelativeLayout(context);
        layout.addView(imgll, getLP(true, false));
        imgll.getLayoutParams().height = getImageHeight();
        imgll.setBackgroundColor(Color.LTGRAY);
        //add image
        if (SDKUtil.isGIF(videoInfo.mediaSourceSmall) || videoInfo.adServing) {
            String url = videoInfo.mediaSourceSmall;
            GIFView gifView = new GIFView(context, listener, videoInfo.type);
            imgll.addView(gifView, getLP(true, false));
            gifView.load(url, videoInfo);
            gifView.getLayoutParams().width = (int) adWidth;
            gifView.getLayoutParams().height = getImageHeight();
            gifView.getCover().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showEndingCard();
                }
            });
        } else {
            String url = SDKUtil.getFileNameFromUrl(context, videoInfo.mediaSourceSmall);
            ImageView imageView = new ImageView(context);
            imgll.addView(imageView, getLP(true, false));
            imageView.getLayoutParams().width = (int) adWidth;
            imageView.getLayoutParams().height = getImageHeight();
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEndingCard();
                }
            });
            bmp = BitmapFactory.decodeFile(url);
            imageView.setImageBitmap(bmp);
        }

        if (!videoInfo.absolute) {
            setEndingCard(imgll);
        }
    }

    private void setEndingCard(ViewGroup parent) {
        int sourceM = LayoutParams.MATCH_PARENT;
        int sourceW = LayoutParams.WRAP_CONTENT;
        int width = (int) (btnh * 1.3);
        if (getViewType() == ViewType.CPMVideo_V) {
            toolBarView = new ToolBarView(context, LinearLayout.HORIZONTAL);
            LayoutParams lp = new LayoutParams(sourceM, sourceW);
            parent.addView(toolBarView, lp);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            toolBarView.getLayoutParams().height = width;
        } else {
            toolBarView = new ToolBarView(context, LinearLayout.VERTICAL);
            LayoutParams lp = new LayoutParams(sourceW, sourceM);
            parent.addView(toolBarView, lp);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            toolBarView.getLayoutParams().width = width;
        }
        toolBarView.setVisibility(View.GONE);
        toolBarView.setItem(videoInfo.endingCard, btnh, new ToolBarView.ItemClickListener() {
            @Override
            public void onClick(int position) {
                listener.endingCardAction(position);
            }
        });
    }

    private void showEndingCard() {
        if (!videoInfo.absolute && toolBarView != null) {
            int tagsCount = 0;
            for (int i = 0; i < videoInfo.endingCard.length; i++) {
                if (videoInfo.endingCard[i])
                    tagsCount++;
            }
            if (tagsCount > 0) {
                toolBarView.setVisibility(View.VISIBLE);
                endingCardDisappear.removeCallbacks(endingCardRunnable);
                if (video.isPlaying()) {
                    endingCardDisappear.postDelayed(endingCardRunnable, 3000);
                }
            }
        } else {
            boolean clicked = false;
            for (int i = 0; i < videoInfo.endingCard.length; i++) {
                if (!clicked && videoInfo.endingCard[i]) {
                    clicked = true;
                    listener.endingCardAction(i);
                }
            }
        }
    }

    Runnable endingCardRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (toolBarView != null)
                    toolBarView.setVisibility(View.GONE);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
        }
    };

    enum CIPosition {
        under_volume, leftAndBottom, leftAndTop
    }

    private CIPosition getCIPostion() {//boolean isNative, boolean isCPM, boolean top, boolean screenPortrait) {
        if (getViewType() == ViewType.CPMVideo_V) {
            return CIPosition.leftAndBottom;
        } else if (getViewType() == ViewType.CPMVideo_H) {
            return CIPosition.under_volume;
        } else if (getViewType() == ViewType.NativeVideo) {
            return CIPosition.under_volume;
        } else if (getViewType() == ViewType.CPV_H || top) {
            return CIPosition.under_volume;
        } else if (getViewType() == ViewType.CPV_V) {
            return CIPosition.leftAndTop;
        }
        return CIPosition.leftAndBottom;
    }

    private boolean isCPM() {
        if (getViewType() == ViewType.CPMVideo_V || getViewType() == ViewType.CPMVideo_H) {
            return true;
        } else {
            return false;
        }
    }

    public ViewGroup.LayoutParams getLP(boolean matchParentW, boolean matchParentH) {
        int mp = ViewGroup.LayoutParams.MATCH_PARENT;
        int wc = ViewGroup.LayoutParams.WRAP_CONTENT;
        int w = matchParentW ? mp : wc;
        int h = matchParentH ? mp : wc;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(w, h);
        return lp;
    }

    private int getVideoHeight() {
        if (getViewType() == ViewType.NativeVideo) {
            return (int) (((float) 360 / (float) 640) * adWidth);
        } else if (isScreenPortrait()) {
            return (int) (((float) 360 / (float) 640) * adWidth);
        } else {
            if (!fullScreen) {
                return (int) adHeight - 3 - SDKUtil.getStatusHeight((Activity) context);
            } else {
                return (int) adHeight - 3;
            }
        }
    }

    private int getImageHeight() {
        if (isScreenPortrait()) {
            int totalH = (int) (((float) 3 / (float) 2) * adWidth);
            return totalH - getVideoHeight();
        } else {
            return 0;
        }
    }

    private int getBgColorAreaHeight() {
        if (isScreenPortrait()) {
            int totalH = (int) (((float) 3 / (float) 2) * adWidth);
            return (int) (((adHeight - (float) totalH)) / 2);
        } else {
            return 0;
        }
    }

    private String getReciprocalStr(int result) {
        if (reciprocalStr.isEmpty()) {
            reciprocalStr = new String(Base64.decode(AdbertParams.reciprocal.getValue(), 0));
        }
        return reciprocalStr.substring(0, 2) + " " + result + " " + reciprocalStr.substring(2);
    }

    StretchVideoView.VideoListener videoListener = new StretchVideoView.VideoListener() {
        @Override
        public void OnCompletion() {
            loadingProgress.setMax(100);
            loadingProgress.setProgress(100);
            reciprocal.setText(getReciprocalStr(0));
            defaultSeekTo = video.getDuration();
            listener.closeAdView();
        }

        @Override
        public void OnError() {
            listener.finish();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            setVolumeStatus();
            if (defaultSeekTo > 0) {
                try {
                    video.seekTo(defaultSeekTo);
                } catch (Exception e) {
                    SDKUtil.logException(e);
                }
                mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        video.start();
                    }
                });
            } else {
                video.start();
            }
        }

        @Override
        public void onSeekChange() {
            if (video.isPlaying()) {
                showLoadingView();
                loadingProgress.setMax(video.getDuration());
                loadingProgress.setProgress(video.getCurrentPosition());
                defaultSeekTo = video.getCurrentPosition();
//                updateCPVSmallVideoTime();
                if (videoInfo != null && video.getCurrentPosition() >= videoInfo.returnTime) {
                    listener.callReturnEvent();
                }
                try {
                    int count = video.getDuration() - video.getCurrentPosition();
                    int result = (int) Math.ceil((double) count / 1000);
                    reciprocal.setText(getReciprocalStr(result));
                } catch (Exception e) {
                    SDKUtil.logException(e);
                }
            }
        }
    };

    private void showLoadingView() {
        if (defaultSeekTo > 0 && preview.getVisibility() == View.VISIBLE) {
            preview.setVisibility(View.GONE);
        }
        if (reciprocal.getVisibility() != View.VISIBLE) {
            reciprocal.setVisibility(View.VISIBLE);
        }
        if (loadingProgress.getVisibility() != View.VISIBLE) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
    }

    public void pause() {
        if (video != null) {
            video.pause();
            preview.setVisibility(View.VISIBLE);
        }
    }

    public void resume() {
        if (video != null) {
            video.start();
        }
    }

    public void destroy() {
        if (video != null) {
            video.destroy();
        }
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
    }
}
