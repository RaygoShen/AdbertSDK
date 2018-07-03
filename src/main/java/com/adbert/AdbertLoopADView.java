package com.adbert;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.adbert.util.SDKUtil;
import com.adbert.util.list.BaseBannerMethod;
import com.adbert.util.list.LoopListener;
import com.adbert.view.StretchVideoView;
import com.google.android.gms.ads.AdSize;

public class AdbertLoopADView extends RelativeLayout implements BaseBannerMethod {
    private Context context;
    private AdbertADView ad;
    private ExpandVideoPosition expandVideoPosition;
    private AdbertOrientation orientation;
    private boolean fullscreen;
    private int bannerWidth = 0;
    private AdSize adSize;
    private String appId = "", appKey = "";
    private AdbertListener listener;
    private Handler timerHandler = new Handler();
    private int bannerShowed = 0;
    private boolean start = false;
    private boolean destroy;
    private String pageInfo = "";

    public AdbertLoopADView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        initView();
    }

    public AdbertLoopADView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        initView();
    }

    public AdbertLoopADView(Context context) {
        super(context);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        initView();
    }

    private void initView() {
        ad = new AdbertADView(context);
        this.addView(ad);
        SDKUtil.firstRequestNonMediationAD = true;
    }

    private void init() {
        if (expandVideoPosition != null)
            ad.setExpandVideo(expandVideoPosition);
        if (orientation != null)
            ad.setMode(orientation);
        ad.setFullScreen(fullscreen);
        if (bannerWidth > 0)
            ad.setBannerSize(bannerWidth);
        if (adSize != null)
            ad.setBannerSize(adSize);
        ad.setNonMediationAPPID(appId, appKey);
        ad.setListener(new LoopListener() {

            @Override
            public void onReceive(String msg) {
                if (listener != null) {
                    listener.onReceive(msg);
                }
            }

            @Override
            public void onFailedReceive(String msg) {
                if (listener != null) {
                    listener.onFailedReceive(msg);
                }
                next(5000);
            }

//            @Override
//            public void onAdClicked() {
//                if(listener!=null){
//                    listener.onAdClicked();
//                }
//            }

            @Override
            public void onCPVEnd() {
                next(0);
            }

            private void next(int ms) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (destroy) {
                            handler.removeCallbacks(this);
                        } else {
                            repeatAgain();
                        }
                    }
                }, ms);
            }
        });

        ad.setPageInfo(pageInfo);
    }

    @Override
    public void setExpandVideo(ExpandVideoPosition expandVideoPosition) {
        this.expandVideoPosition = expandVideoPosition;
    }

    @Override
    public void setMode(AdbertOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public void setFullScreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public void setBannerSize(int bannerWidth) {
        this.bannerWidth = bannerWidth;
    }

    @Override
    public void setBannerSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public void setAPPID(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
    }

    @Override
    public void setListener(AdbertListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if (!start) {
            start = true;
            run();
        }
    }

    private void run() {
        init();
        ad.start();
        setTimer();
        if (show) {
            timerHandler.postDelayed(timer, 1000);
        }
    }

    private void repeatAgain() {
        bannerShowed = 0;
        ad.destroy();
        AdbertLoopADView.this.removeView(ad);
        ad = new AdbertADView(context);
        AdbertLoopADView.this.addView(ad);
        run();
    }

    int loopTime = 30000;

    private void setTimer() {
        if (timer != null)
            timerHandler.removeCallbacks(timer);
        timer = new Runnable() {

            @Override
            public void run() {
                bannerShowed += 1000;
                if (destroy) {
                    timerHandler.postDelayed(this, 1000);
                } else if (bannerShowed >= loopTime && !isDemo) {
                    bannerShowed = 0;
                    repeatAgain();
                    timerHandler.removeCallbacks(this);
                } else {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    Runnable timer = null;

    @Override
    public void destroy() {
        destroy = true;
        timerHandler.removeCallbacks(timer);
        ad.destroy();
    }

    @Override
    public void pause() {
        timerHandler.removeCallbacks(timer);
        ad.pause();
    }

    @Override
    public void resume() {
        if (show) {
            setTimer();
            timerHandler.postDelayed(timer, 1000);
            ad.resume();
        }
    }

    boolean show = true;

    public void hideView() {
        show = false;
        pause();
        handleVideo(false);
        this.setVisibility(View.GONE);
    }

    public void showView() {
        show = true;
        this.setVisibility(View.VISIBLE);
        handleVideo(true);
        resume();
    }

    private void handleVideo(boolean show) {
        StretchVideoView video = (StretchVideoView) ad.findViewWithTag("adbert_video");
        if (video != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (show) {
                    video.setAlpha(1f);
                } else {
                    video.setAlpha(0);
                }
            }
        }
    }

    @Override
    public void hideCI() {
        ad.hideCI();
    }

    @Override
    public String getVersion() {
        return ad.getVersion();
    }

    @Override
    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    boolean isDemo = false;

//    @Override
//    public void startForDemo(String jsonStr) {
//        this.jsonStr = jsonStr;
//        isDemo = true;
//        run();
//    }

    @Override
    public void setTestMode() {
        ad.setTestMode();
    }


}
