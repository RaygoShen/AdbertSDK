package com.adbert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adbert.util.ConnectionManager;
import com.adbert.util.CustomConnection;
import com.adbert.util.NativeDataParser;
import com.adbert.util.ParamsControl;
import com.adbert.util.ReturnDataUtil;
import com.adbert.util.SDKUtil;
import com.adbert.util.ToolBarAction;
import com.adbert.util.data.CommonNativeAD;
import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.LogMsg;
import com.adbert.util.enums.ShareType;
import com.adbert.util.list.CustomViewListener;
import com.adbert.view.ExpandVideoView;
import com.adbert.view.TrackingView;

/**
 * Created by chihhan on 16/10/3.
 */
public class AdbertVideoBox extends RelativeLayout {

    private String appId = "", appKey = "", pageInfo = "";
    private AdbertVideoBoxListener listener;
    private boolean isTestMode = false;
    private CommonNativeAD nativeAD;
    private int btnh = 50;
    private int pWidth = 0, pHeight = 0;
    private boolean screenPortrait;
    private String uuId = "";
    private ExpandVideoView expandVideo;
    private Context context;

    public AdbertVideoBox(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AdbertVideoBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public AdbertVideoBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        init();
    }

    private void init() {
        screenPortrait = SDKUtil.isPortrait(context);
        btnh = SDKUtil.getBtnWidth(context, screenPortrait, btnh);
    }

    public void setID(String appId, String appKey) {
        this.appId = appId.trim();
        this.appKey = appKey.trim();
    }

    public void setTestMode() {
        isTestMode = true;
    }

    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    public void setListener(AdbertVideoBoxListener listener) {
        this.listener = listener;
    }

    public void loadAD() {
        this.removeAllViews();
        if (appId.isEmpty() || appKey.isEmpty()) {
            returnFail(LogMsg.ERROR_ID_NULL.getValue());
        } else if (!SDKUtil.connectable(getContext())) {
            returnFail(LogMsg.ERROR_CONNECTION.getValue());
        } else {
            SDKUtil.getUUID(context, new SDKUtil.GetUUIDListener() {
                @Override
                public void onResult(String result) {
                    uuId = result;
                    String testMode = isTestMode ? "" : "&testMode=1";
                    String paramsStr = new ParamsControl(getContext()).getNativeADParams(appId, appKey, "", "native_video",
                            pageInfo) + testMode + AdbertParams.uuid.AndSetValue(uuId);
                    ConnectionManager.getInstance().newSimpleConnection(context, AdbertParams.nativeADURL.getHashURL(uuId), paramsStr, new ConnectionManager.SimpleConnectionListener() {
                        @Override
                        public void onEnd(int code, String result) {
                            requestFinished(code, result);
                        }
                    });
                }
            });
        }
    }

    private void returnSuccess(final String msg) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onReceived(msg);
                SDKUtil.logInfo(msg);
            }
        });
    }

    private void returnFail(final String msg) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onFailReceived(msg);
                SDKUtil.logWarning(msg);
            }
        });
    }

    private void requestFinished(int responseCode, String jsonStr) {
        if (responseCode != 200) {
            returnFail(LogMsg.ERROR_SERVICE.getValue());
        } else if (jsonStr != null && jsonStr.isEmpty()) {
            returnFail(LogMsg.ERROR_JSON_EMPTY.getValue());
        } else if (jsonStr != null && !jsonStr.isEmpty()) {
            nativeAD = new NativeDataParser(getContext(), jsonStr, "native_video").getResult();
            nativeAD.uuId = uuId;
            if (nativeAD == null) {
                returnFail(LogMsg.ERROR_JSON_PARSE.getValue());
            } else if (nativeAD.mediaSrc.isEmpty()) {
                returnFail(LogMsg.ERROR_JSON_PARSE.getValue());
            } else {
                String path = SDKUtil.getFileNameFromUrl(context, nativeAD.mediaSrc);
                ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
                    @Override
                    public void onConnectionSuccess(CustomConnection cc) {
                        returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
                        setNativeVideoView();
                    }

                    @Override
                    public void onConnectionFail(CustomConnection cc) {
                        returnFail(LogMsg.ERROR_DOWNLOAD_FILE.getValue());
                    }
                }).getFileAndSave(nativeAD.mediaSrc, path);
            }
        }
    }

    private void setNativeVideoView() {
        if (!nativeAD.gaUrl.isEmpty()) {
            setTrackingView();
        }
        expandVideo = new ExpandVideoView(context, nativeAD.getCommonAD(), btnh, viewListener);
        expandVideo.setSize(this.getMeasuredWidth(), pHeight);
        expandVideo.showNativeVideo();
//        expandVideo = new ExpandVideoView(getContext(), nativeAD, btnh,
//                this.getMeasuredWidth(), pHeight, viewListener);
        this.addView(expandVideo);
        ReturnDataUtil.exposureEvent(context, nativeAD.getCommonAD());
    }

    private TrackingView trackingView;

    private void setTrackingView() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ADBERT", 0);
        if (sharedPreferences.getBoolean("exise", false) && context != null) {
            sharedPreferences.edit().putBoolean("exist", true).apply();
            trackingView = new TrackingView(context);
            this.addView(trackingView);
            trackingView.loadUrl(nativeAD.gaUrl);
        }
    }

    CustomViewListener viewListener = new CustomViewListener() {

        public void setLogo(ViewGroup parent, boolean isRLayout) {
            if (screenPortrait) {
                SDKUtil.setLogo(getContext(), (int) (pWidth * SDKUtil.ciScale), parent, isRLayout);
            } else {
                SDKUtil.setLogo(getContext(), (int) (pHeight * SDKUtil.ciScale), parent, isRLayout);
            }
        }

        public void endingCardAction(int position) {
            AdbertVideoBox.this.endingCardAction(position);
        }

        public void closeAdView() {
            listener.onCompletion();
        }

        public void callReturnEvent() {
            returnClick();
        }

    };

    private void endingCardAction(int type) {
        if (!nativeAD.returned) {
            nativeAD.returned = true;
            returnClick();
        }
        returnShare(ShareType.init.fromInt(type));
//        if (listener != null && SDKUtil.returnClickEvent) {
//            listener.onAdClicked();
//        }
        ToolBarAction.getToolBar(context).toolbarAction(nativeAD.getCommonAD(), type, null);
    }

    private void returnClick() {
        if (!nativeAD.returned && !nativeAD.returnUrl.isEmpty()) {
            nativeAD.returned = true;
            String paramsStr = new ParamsControl(getContext()).getReturnParams(nativeAD.appId,
                    nativeAD.appKey, uuId, nativeAD.pid);
            ConnectionManager.getInstance().newSimpleConnection(context, nativeAD.returnUrl, paramsStr, new ConnectionManager.SimpleConnectionListener() {
                @Override
                public void onEnd(int code, String result) {
                    nativeAD.returned = (code == 200);
                }
            });
        }
    }

    private void durationReturn(int seconds) {
        String paramsStr = new ParamsControl(getContext()).getSecondsParams(nativeAD.appId,
                nativeAD.appKey, uuId, nativeAD.pid, seconds);
        ConnectionManager.getInstance().newSimpleConnection(context, nativeAD.durationReturnUrl, paramsStr);
    }

    private void returnShare(String shareType) {
        String paramsStr = new ParamsControl(getContext()).getShareParams(nativeAD.appId,
                nativeAD.appKey, uuId, shareType, nativeAD.pid, "");
        ConnectionManager.getInstance().newSimpleConnection(context, nativeAD.shareRetrunUrl, paramsStr);
    }

    public void destroy() {
        if (expandVideo != null) {
            if (expandVideo.getSeekTo() > 0)
                durationReturn(expandVideo.getSeekTo());
            expandVideo.destroy();
        }
        if (trackingView != null) {
            trackingView.destroy();
        }
    }

    public void pause() {
        if (expandVideo != null) {
            expandVideo.pause();
        }
    }

    public void resume() {
        if (expandVideo != null) {
            expandVideo.resume();
        }
    }
}
