package com.adbert;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.adbert.util.ReturnDataUtil;
import com.adbert.util.SDKUtil;
import com.adbert.util.ToolBarAction;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.ActionType;
import com.adbert.util.enums.Colors;
import com.adbert.util.enums.ShareType;
import com.adbert.util.list.CustomViewListener;
import com.adbert.view.AdbertWebView;
import com.adbert.view.ExpandVideoView;

public class AdbertActivity extends Activity {

    private int btnh = 50, act_type = -1;
    private boolean landMode = false, screenPortrait;
    private float pWidth, pHeight;
    private Object[] datas;
    private CommonData videoInfo;
    private AdbertWebView webViewll;
    private WebView webView;
    private ExpandVideoView expandVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        init();
        if (checkStatus()) {
            if (act_type == ActionType.act_web.getValue()) {
                showWeb(getIntent().getExtras().getString("url"));
            } else if (act_type == ActionType.act_video2.getValue()) {
                showCPV();
            }
        } else {
            broadcast("next", new Bundle());
        }
    }

    private void broadcast(String action, Bundle bundle) {
        Intent intent = new Intent("ad" + videoInfo.pid);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        bundle.putString("action", action);
        intent.putExtras(bundle);
        broadcastManager.sendBroadcast(intent);
    }

    boolean fullScreen = true;

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        if (getIntent().hasExtra("fullScreen"))
            fullScreen = getIntent().getExtras().getBoolean("fullScreen");
        pWidth = dm.widthPixels;
        pHeight = dm.heightPixels;
//		pDensity = dm.density;
        videoInfo = (CommonData) getIntent().getSerializableExtra("videoInfo");
        datas = (Object[]) getIntent().getExtras().get("datas");
        landMode = (Boolean) datas[1];
        act_type = (Integer) datas[3];
        screenPortrait = SDKUtil.isPortrait(this);
        btnh = SDKUtil.getBtnWidth(this, screenPortrait, btnh);
        if (fullScreen || (!screenPortrait && act_type == ActionType.act_video2.getValue())) {
            fullScreen = true;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        int adbertOrientation = getIntent().getExtras().getInt("adbertOrientation");
        if (adbertOrientation == AdbertOrientation.NORMAL.getValue()) {
            landMode = !screenPortrait;
        }
    }

    private boolean checkStatus() {
        return (landMode == !screenPortrait);
    }

    private void showCPV() {
        final RelativeLayout layout = new RelativeLayout(this);
        if (!getIntent().hasExtra("hideCI"))
            layout.setBackgroundColor(Colors.videoBg.parseColor());
        else
            layout.setBackgroundColor(Colors.cpmBgLight.parseColor());
        setContentView(layout);
        boolean top = getIntent().getExtras().getBoolean("top");
        int seekTo = getIntent().getExtras().getInt("seekTo");
        expandVideo = new ExpandVideoView(this, videoInfo, btnh, viewListener);
        expandVideo.showCPV(seekTo, top, fullScreen);
        layout.addView(expandVideo);
    }

    private void showWeb(String url) {
        pause();
        webViewll = new AdbertWebView(this, videoInfo, viewListener).load(url, true, btnh);
        if (act_type == ActionType.act_web.getValue()) {
            setContentView(webViewll);
        } else if (expandVideo != null && expandVideo.getVisibility() == View.VISIBLE) {
            expandVideo.addView(webViewll);
        } else
            webView = webViewll.getWebView();
    }

    CustomViewListener viewListener = new CustomViewListener() {

        public void setLogo(ViewGroup parent, boolean isRLayout) {
            if (!videoInfo.special) {
                float pw = screenPortrait ? pWidth : pHeight;
                SDKUtil.setLogo(getApplicationContext(), (int) (pw * SDKUtil.ciScale), parent, isRLayout);
            }
        }

        public void finish() {
            AdbertActivity.this.finish();
        }

        public void endingCardAction(int position) {
            AdbertActivity.this.endingCardAction(position);
        }

        public void closeWeb() {
            if (act_type == ActionType.act_web.getValue()) {
                finish();
            } else {
                expandVideo.removeView(webViewll);
                webViewll = null;
                resume();
            }
        }

        public void closeVideo() {
            closeAndResume();
        }

        public void closeAdView() {
            AdbertActivity.this.closeAndResume();
        }

        public void callReturnEvent() {
            if (!videoInfo.returned) {
                videoInfo.returned = true;
                ReturnDataUtil.returnEvent(getApplicationContext(), videoInfo, new Runnable() {

                    @Override
                    public void run() {
                        videoInfo.returned = false;
                    }
                });
            }
        }

    };

    private void pause() {
        if (expandVideo != null)
            expandVideo.pause();
    }

    private void resume() {
        if (expandVideo != null) {
            expandVideo.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (expandVideo != null) {
            expandVideo.destroy();
        }
        if (webView != null)
            webView.destroy();
    }

    private void endingCardAction(int type) {
        //return valid & share
        ShareType shareType = ShareType.init.getTypeFromPosition(type);
        if (!videoInfo.returned) {
            videoInfo.returned = true;
            ReturnDataUtil.returnEvent(this, videoInfo, new Runnable() {

                @Override
                public void run() {
                    videoInfo.returned = false;
                }
            });
        }
        ReturnDataUtil.shareReturn(this, videoInfo, shareType.toString());
        broadcast("click", new Bundle());
        //action
        ToolBarAction.getToolBar(this).toolbarAction(videoInfo, type, new ToolBarAction.OpenInAppListener() {
            @Override
            public void open(String url) {
                showWeb(url);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (act_type == ActionType.act_web.getValue()) {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else
                    closeAndResume();
            } else if (webViewll != null && webViewll.getVisibility() == View.VISIBLE) {
                if (expandVideo != null) {
                    expandVideo.removeView(webViewll);
                }
                webViewll = null;
                resume();
            } else {
                closeAndResume();
            }
            return false;
        } else
            return super.dispatchKeyEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!landMode) {
                closeAndResume();
            } else
                finish();
            landMode = true;
            screenPortrait = false;
        } else {
            if (landMode) {
                closeAndResume();
            } else {
                finish();
            }
            landMode = false;
            screenPortrait = true;
        }
    }

    private void closeAndResume() {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("data", videoInfo);
//        broadcast("dataChange", bundle);
        if (act_type == ActionType.act_video2.getValue()) {
            Bundle bundle2 = new Bundle();
            bundle2.putInt("type", act_type);
            bundle2.putInt("seekTo", expandVideo.getSeekTo());
            bundle2.putBoolean("returned", videoInfo.returned);
            broadcast("close", bundle2);
        }
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
