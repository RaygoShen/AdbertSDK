package com.adbert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adbert.util.BannerSize;
import com.adbert.util.BeaconScanner;
import com.adbert.util.ConnectionManager;
import com.adbert.util.CustomConnection;
import com.adbert.util.DataParser;
import com.adbert.util.ParamsControl;
import com.adbert.util.ReturnDataUtil;
import com.adbert.util.SDKUtil;
import com.adbert.util.ToolBarAction;
import com.adbert.util.WebkitCookieManagerProxy;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.ActionType;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.LogMsg;
import com.adbert.util.enums.ShareType;
import com.adbert.util.list.BaseBannerMethod;
import com.adbert.util.list.CustomViewListener;
import com.adbert.util.list.LoopListener;
import com.adbert.view.AdbertWebView;
import com.adbert.view.GIFView;
import com.adbert.view.LogoImage;
import com.adbert.view.StretchVideoView;
import com.adbert.view.TrackingView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ResourceType")
public class AdbertADView extends RelativeLayout implements BaseBannerMethod {

    private Context context;
    private String uuId = "", appId = "", appKey = "";
    private boolean putVideo2AtTop, leavePage, fullScreen = true;
    private boolean firstGetData = true, firstRequestData = true, bigedInThisOpen, hideCI;
    private int seekTo = 0;
    private CommonData ad = new CommonData();
    private StretchVideoView video;
    private Bitmap bmp_s;
    private FrameLayout videoll;
    private boolean alreadyReturnStatus = false;
    private AdbertListener listener;
    private boolean destroy;
    private boolean started = false;
    private boolean isMediation = true;
    private BannerSize size;
    private String pageInfo = "";
    private boolean isTestMode = false;
    private BeaconScanner scanner;

    @Override
    public void setTestMode() {
        isTestMode = true;
    }

    @Override
    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    public AdbertADView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public AdbertADView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public AdbertADView(Context context) {
        super(context);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        init();
    }

    @Override
    public String getVersion() {
        return SDKUtil.version;
    }

    private void init() {
        SDKUtil.logInfo(AdbertParams.Version.toString());
        cleanCacheFolder();
        size = new BannerSize(context);
        size.setLandMode(!size.isScreenPortrait());
        initCookie();
    }

    private void initCookie() {
        android.webkit.CookieSyncManager.createInstance(context);
        // unrelated, just make sure cookies are generally allowed
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        // magic starts here
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null,
                java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
    }

    @Override
    public void setBannerSize(int bannerWidth) {
        size.setBannerWidth(bannerWidth);
    }

    AdSize adSize = AdSize.BANNER;

    @Override
    public void setBannerSize(AdSize adSize) {
        this.adSize = adSize;
        int w = adSize.getWidthInPixels(context);
        int h = adSize.getHeightInPixels(context);
        size.setBannerWidth(w);
        if (adSize == AdSize.BANNER || (float) w / (float) h <= 6.4) {
        } else {
            size.setItemSize(h);
        }
    }

    @Override
    public void setExpandVideo(ExpandVideoPosition position) {
        if (position == ExpandVideoPosition.TOP) {
            putVideo2AtTop = true;
        } else {
            putVideo2AtTop = false;
        }
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    AdbertOrientation orientation = AdbertOrientation.NORMAL;

    @Override
    public void setMode(AdbertOrientation orientation) {
        this.orientation = orientation;
        if (orientation == AdbertOrientation.LAND) {
            size.setLandMode(true);
        } else if (orientation == AdbertOrientation.PORT) {
            size.setLandMode(false);
        } else if (orientation == AdbertOrientation.NORMAL) {
            size.setLandMode(!size.isScreenPortrait());
        }
    }

    @Override
    public void setListener(AdbertListener listener) {
        this.listener = listener;
    }

    public void setNonMediationAPPID(String appId, String appKey) {
        this.appId = appId.trim();
        this.appKey = appKey.trim();
        isMediation = false;
    }

    @Deprecated
    public void setAPPID(String appId, String appKey) {
        setMediationAPPID(appId + "|" + appKey);
    }

    public void setMediationAPPID(String serverParameter) {
        if (serverParameter.contains("|")) {
            this.appId = serverParameter.substring(0, serverParameter.indexOf("|")).trim();
            this.appKey = serverParameter.substring(serverParameter.indexOf("|") + 1).trim();
            isMediation = true;
        }
    }

    @Override
    public void start() {
        if (checkStatus() && !started && !appId.isEmpty() && !appKey.isEmpty()) {
            started = true;
            setView();
            SDKUtil.getUUID(context, new SDKUtil.GetUUIDListener() {
                @Override
                public void onResult(String result) {
                    uuId = result;
                    if (checkStatus()) {
                        SDKUtil.logInfo(LogMsg.START.getValue());
                        loadAd();
                    } else {
                        returnFail(LogMsg.ERROR_MODE.getValue());
                        AdbertADView.this.setVisibility(View.GONE);
                    }
                }
            });
        } else if (appId.isEmpty() || appKey.isEmpty()) {
            returnFail(LogMsg.ERROR_ID_NULL.getValue());
        }
    }

    @Override
    public void pause() {
        leavePage = true;
        if (checkStatus()) {
            if (ad != null && ad.type == AdbertADType.video) {
                if (video != null) {
                    seekTo = video.getCurrentPosition();
                    video.pause();
                }
            }
            if (adView != null) {
                adView.pause();
            }
        }

        if (scanner != null) {
            scanner.stopScan();
        }
    }

    @Override
    public void resume() {
        if (leavePage) {
            leavePage = false;
            if (checkStatus()) {
                if (ad != null && ad.type == AdbertADType.video && video != null
                        && video.getVisibility() == View.VISIBLE) {
                    ad.goWhere = 1;
                    show();
                }
            }
            if (adView != null) {
                adView.resume();
            }
        }

        if (scanner != null) {
            scanner.startScan();
        }
    }

    @Override
    public void destroy() {
        destroy = true;
        //return scan result
        if (scanner != null && ad != null) {
            String result = scanner.getResult(ad.uuId);
            ReturnDataUtil.iBeaconReturn(context, result, ad);
            scanner.stopScan();
            scanner = null;
        }
        destroyView();
        if (context != null) {
            context = null;
        }
    }

    private void destroyView() {
        if (video != null) {
            video.pause();
            video.stopPlayback();
            video = null;
        }
        if (bmp_s != null && !bmp_s.isRecycled()) {
            bmp_s.recycle();
            bmp_s = null;
        }
        if (trackingView != null) {
            trackingView.destroy();
            trackingView = null;
        }
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (ad != null && ad.type == AdbertADType.video) {
            if (seekTo > 0) {
                ReturnDataUtil.durationReturn(context, ad, seekTo);
            }
        }
        if (videoll != null) {
            videoll = null;
        }
        if (ad != null) {
            ad = null;
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        this.removeAllViews();
    }

    @Override
    public void hideCI() {
        hideCI = true;
    }

    private void setObjSize(View object) {
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        object.setLayoutParams(lp);
        object.getLayoutParams().width = size.getItemWidth();
        object.getLayoutParams().height = size.getItemHeight();
        object.setVisibility(View.GONE);
    }

    private TrackingView trackingView;

    private void loadTrackingUrl() {
        if (context != null && trackingView != null && !ad.gaUrl.isEmpty()) {
            trackingView.loadUrl(ad.gaUrl);
        }
    }

    private void setView() {
        trackingView = new TrackingView(getContext());
        this.addView(trackingView);
        setGravity(Gravity.CENTER);
        // Videoll
        videoll = new FrameLayout(context);
        this.addView(videoll);
        setObjSize(videoll);
        videoll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ad.type == AdbertADType.video && video != null && video.isPlaying()) {
                    openActivity_video();
                }
            }
        });
    }

//    LogoImage logo;

    private String getOtherParams(ParamsControl paramsControl) {
        if (!isMediation && !SDKUtil.firstRequestNonMediationAD) {
            firstRequestData = false;
        }
        String paramStr = paramsControl.getBannerParams(appId, appKey, uuId,
                AdbertParams.COMMON_AD.toString(), size.isScreenPortrait(), firstRequestData, pageInfo)
                + paramsControl.getNormalParams();
        return paramStr;
    }

    private void loadAd() {
        if (SDKUtil.connectable(context)) {
            seekTo = 0;
            bmp_s = null;
            ParamsControl paramsControl = new ParamsControl(context);
            String testMode = "";
            if (isTestMode) testMode = "&testMode=1";
            final String paramStr = getOtherParams(paramsControl) + testMode;
            String url = AdbertParams.ADURL.getHashURL(uuId);
            Log.v("TEST1211","url = " + url + " paramStr = " + paramStr);
            //request
            ConnectionManager.getInstance().newSimpleConnection(context, url, paramStr, new ConnectionManager.SimpleConnectionListener() {
                @Override
                public void onEnd(int code, String result) {
                    result = result.trim();
                    if (code != 200) {
                        returnFail(LogMsg.ERROR_SERVICE.getValue());
                    } else if (code == 200 && result.isEmpty()) {
                        returnFail(LogMsg.ERROR_JSON_EMPTY.getValue());
                    } else if (code == 200 && !result.isEmpty()) {
                        firstRequestData = false;
                        SDKUtil.firstRequestNonMediationAD = false;
                        setDatas(result);
                    }
                }
            });
        } else {
            returnFail(LogMsg.ERROR_CONNECTION.getValue());
        }
    }

    @SuppressWarnings("ResourceType")
    public void setAdmob() throws Exception {
        if (!destroy && context != null) {
            adView = new AdView(context);
            adView.setAdUnitId(AdbertParams.bannerID.getValue());
            adView.setAdSize(adSize);
            size.setBannerWidth(adSize.getWidthInPixels(context));
            this.addView(adView);
            adView.getLayoutParams().width = size.getAdbertWidth();
            adView.getLayoutParams().height = size.getAdbertHeight();
            ((LayoutParams) adView.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {

                public void onAdLoaded() {
                    returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
                }

                public void onAdFailedToLoad(int errorCode) {
                    adView.destroy();
                    adView = null;
//                returnFail(LogMsg.ERROR_JSON_EMPTY.getValue());
                    String msg = LogMsg.ERROR_JSON_EMPTY.getValue();
                    listener.onFailedReceive(msg);
                    alreadyReturnStatus = true;
                    SDKUtil.logWarning(msg);
                }
            });
            adView.loadAd(adRequest);
        }
    }

    private void setDatas(String jsonStr) {
        ad = new CommonData();
        try {
            ad = new DataParser(context).parse(jsonStr, size.isScreenPortrait());
            ad.uuId = uuId;
            if (ad.type == null) {
                returnFail(LogMsg.ERROR_TYPE_NULL.getValue());
            } else if (ad.type == AdbertADType.video && SDKUtil.isSDKunder14()) {
                ReturnDataUtil.exposureEvent_forUnderV14Video(context, ad, new Runnable() {

                    @Override
                    public void run() {
                        AdbertADView.this.returnFail(LogMsg.VIDEO_NOT_SUPPORT.getValue());
                    }
                });
            } else if (checkStatus()) {
                checkAndStartDownload();
            }
        } catch (Exception e) {
            SDKUtil.logException(e);
            if (e.getMessage() != null)
                returnFail(LogMsg.ERROR_JSON_PARSE.getValue() + e.getMessage());
            else {
                returnFail(LogMsg.ERROR_JSON_PARSE.getValue());
                e.printStackTrace();
            }
        }
    }

    private void returnExposure() {
        if (firstGetData) {
            ReturnDataUtil.exposureEvent(context, ad);
            firstGetData = false;
        }
    }

    private void checkAndStartDownload() {
        if (isMediation && SDKUtil.isAllus()) {
            returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
        }
        // check data string
        boolean status = false;
        if (ad.type == AdbertADType.video
                && SDKUtil.checkResourceString(ad.mediaSource, ad.mediaSourceSmall)) {
            status = true;
        } else if (ad.type == AdbertADType.banner && SDKUtil.checkResourceString(ad.banner)) {
            status = true;
        } else if (ad.type == AdbertADType.banner_web) {
            status = true;
        }
        if (!status) {
            returnFail(LogMsg.ERROR_RESOURCE_FORMAT.getValue());
        } else {
            if (ad.type == AdbertADType.video) {
                ad.goWhere = firstRunCPV() ? 2 : 1;
                downloadVideos(ad.mediaSource, ad.mediaSourceSmall);
            } else if (ad.type == AdbertADType.banner && !SDKUtil.isGIF(ad.banner)) {
                downloadBanner();
            } else {
                show();
            }
        }
    }

    private void downloadVideos(final String mediaURL, final String mediaSmallURL) {
        ConnectionManager.ConnectionListener listener = new ConnectionManager.ConnectionListener() {
            @Override
            public void onConnectionSuccess(CustomConnection cc) {
                if (!destroy) {
                    resultCount++;
                    successCount++;
                    end();
                }
            }

            @Override
            public void onConnectionFail(CustomConnection cc) {
                if (!destroy) {
                    resultCount++;
                    end();
                }
            }

            int resultCount = 0;
            int successCount = 0;

            private void end() {
                if (resultCount == 2) {
                    if (successCount == 2) {
                        returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
                        show();
                    } else {
                        returnFail(LogMsg.ERROR_DOWNLOAD_FILE.getValue());
                    }
                }
            }
        };
        ConnectionManager cm = ConnectionManager.getInstance();
        String savePath = SDKUtil.getFileNameFromUrl(context, mediaURL);
        String savePath2 = SDKUtil.getFileNameFromUrl(context, mediaSmallURL);
        cm.newConnection(context).setListener(listener).getFileAndSave(mediaURL, savePath);
        cm.newConnection(context).setListener(listener).getFileAndSave(mediaSmallURL, savePath2);
    }

    private void show() {
        if (ad != null) {
            AdbertADType type = ad.type;
            returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
            if (type == AdbertADType.banner_web) {
                cpmWebView = new AdbertWebView(context, ad, webListener).load(
                        ad.creativeUrl, true, 0, true);
                this.addView(cpmWebView);
                cpmWebView.getLayoutParams().width = size.getItemWidth();
                cpmWebView.getLayoutParams().height = size.getItemHeight();
                cpmWebView.setVisibility(View.VISIBLE);
            } else if (type == AdbertADType.banner) {
                loadTrackingUrl();
                if (SDKUtil.isGIF(ad.banner)) {
                    GIFView gifView = new GIFView(context, webListener, ad.type);
                    this.addView(gifView);
                    setObjSize(gifView);
                    gifView.load(ad.banner, ad);
                    gifView.setVisibility(View.VISIBLE);
                } else {
                    // Banner
                    ImageView banner = new ImageView(context);
                    this.addView(banner);
                    banner.setScaleType(ImageView.ScaleType.FIT_XY);
                    setObjSize(banner);
                    banner.setVisibility(View.VISIBLE);
                    banner.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (ad.type == AdbertADType.banner) {
                                for (int i = 0; i < 5; i++) {
                                    if (ad.endingCard[i]) {
                                        endingCardAction(i);
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    banner.setImageBitmap(bmp_s);
                }
            } else if (type == AdbertADType.video) {
                showCPV(ad.goWhere);
            }
            if (!ad.special) {
                addLogo();
            }

            returnExposure();

            //start  scanner
            if (ad.isRunScanner(context)) {
                scanner = new BeaconScanner(ad.iBeacons);
                scanner.startScan();
            }
        }
    }

    private void addLogo() {
        // Logo
        String logoTag = "adbert_log";
        LogoImage logo = (LogoImage) this.findViewWithTag(logoTag);
        if (logo == null) {
            logo = new LogoImage(context, (int) (size.getItemWidth() * SDKUtil.ciScale));
            this.addView(logo);
            logo.setTag(logoTag);
        }
        ((LayoutParams) logo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    }

    AdView adView;
    AdbertWebView cpmWebView;

    private void downloadBanner() {
        ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
            @Override
            public void onConnectionSuccess(CustomConnection cc) {
                if (!destroy) {
                    try {
                        String savePath = SDKUtil.getFileNameFromUrl(context, cc.getFinalUrl());
                        boolean b = SDKUtil.savePic(cc.getBitmap(), ad.banner, cc.getFinalUrl(), savePath);
                        bmp_s = cc.getBitmap();
                        if (b) {
                            returnSuccess(LogMsg.OK_DOWNLOAD.getValue());
                            show();
                        } else {
                            returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
                        }
                    } catch (Exception e) {
                        SDKUtil.logException(e);
                        returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
                    }
                }
            }

            @Override
            public void onConnectionFail(CustomConnection cc) {
                if (!destroy) {
                    returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
                }
            }
        }).getImage(ad.banner);
    }

    private void showCPV(int goWhere) {
        if (goWhere == 1 || (goWhere == 2 && ad.type == AdbertADType.video)) {
            if (videoll != null) {
                videoll.setVisibility(View.VISIBLE);
                loadTrackingUrl();
            }
        }
        if (goWhere == 1) {
            if (video == null) {
                video = new StretchVideoView(context, size.getItemWidth(), size.getItemHeight());
                video.setTag("adbert_video");
                video.setListener(new StretchVideoView.VideoListener() {
                    @Override
                    public void OnCompletion() {
                        if (listener instanceof LoopListener) {
                            ((LoopListener) listener).onCPVEnd();
                        }
                    }

                    @Override
                    public void OnError() {
                        returnFail(LogMsg.ERROR_DOWNLOAD_FILE.getValue());
                        File file = new File(SDKUtil.getFileNameFromUrl(context, ad.mediaSourceSmall));
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        video.volumeClose();
                        if (seekTo > 0) {
                            video.pause();
                            video.seekTo(seekTo);
                            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

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
                            if (video.getVisibility() != View.VISIBLE) {
                                video.setVisibility(View.VISIBLE);
                            }
                            if (video.getCurrentPosition() > 0 && cover != null
                                    && cover.getVisibility() == View.VISIBLE) {
                                cover.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
            if (video != null && video.getParent() != null) {
                hideVideoAndRelease();
            }
            String path = ad.mediaSourceSmall;
            if (path.contains("http") && new File(SDKUtil.getFileNameFromUrl(context, path)).exists()) {
                path = SDKUtil.getFileNameFromUrl(context, path);
            }
            video.setUrl(path);
            videoll.addView(video);
            video.setVisibility(View.VISIBLE);
            cover = new FrameLayout(context);
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            videoll.addView(cover, lp2);
            cover.setBackgroundColor(Color.BLACK);
            if (hideCI)
                video.setZOrderOnTop(true);
        } else if (goWhere == 2)

        {
            openActivity_video();
        }
    }

    FrameLayout cover;

    private void returnSuccess(String msg) {
        if (!alreadyReturnStatus && listener != null) {
            listener.onReceive(msg);
            alreadyReturnStatus = true;
            SDKUtil.logInfo(msg);
        }
    }

    private void returnFail(String msg) {
        if (SDKUtil.isAllus()) {
            try {
                destroyView();
                setAdmob();
            } catch (Exception e) {
                SDKUtil.logException(e);
                listener.onFailedReceive(msg);
                alreadyReturnStatus = true;
                SDKUtil.logWarning(msg);
            }
        } else if (!alreadyReturnStatus && listener != null) {
            listener.onFailedReceive(msg);
            alreadyReturnStatus = true;
            SDKUtil.logWarning(msg);
        }
    }

    private boolean firstRunCPV() {
        if (ad.shouldOpen) {
            if (!bigedInThisOpen) {
                bigedInThisOpen = true;
                return true;
            } else {
                SharedPreferences settings = context.getSharedPreferences(context.getPackageName(), 0);
                String lastDate = settings.getString("lastRunCPV", "");
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Calendar c = Calendar.getInstance();
                String date = df.format(c.getTime());
                if (lastDate.isEmpty() || Integer.parseInt(lastDate) < Integer.parseInt(date)) {
                    settings.edit().putString("lastRunCPV", date).commit();
                    bigedInThisOpen = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void hideVideoAndRelease() {
        if (video != null) {
            video.pause();
            video.stopPlayback();
        }
        if (videoll != null) {
            videoll.removeAllViews();
        }
        //hide logo
        String logoTag = "adbert_log";
        LogoImage logo = (LogoImage) this.findViewWithTag(logoTag);
        logo.setVisibility(View.GONE);
    }

    private void endingCardAction(int type) {
        //return valid & share
        ShareType shareType = ShareType.init.getTypeFromPosition(type);

        if (!ad.returned) {
            ad.returned = true;
            ReturnDataUtil.returnEvent(context, ad, new Runnable() {

                @Override
                public void run() {
                    ad.returned = false;
                }
            });
        }
        ReturnDataUtil.shareReturn(context, ad, shareType.toString());
//        if (listener != null && SDKUtil.returnClickEvent) {
//            listener.onAdClicked();
//        }
        ToolBarAction.getToolBar(context).toolbarAction(ad, type, new ToolBarAction.OpenInAppListener() {
            @Override
            public void open(String url) {
                openActivity_web(url);
            }
        });
    }

    private Intent getIE(int type, Object... datas) {
        Intent ie = new Intent(context.getApplicationContext(), AdbertActivity.class);
        Bundle bundle = new Bundle();
        ie.putExtra("datas", new Object[]{uuId, size.isLandMode(), context.getPackageName(), type});
        if (type == ActionType.act_web.getValue()) {
            ie.putExtra("url", (String) datas[0]);
        } else if (type == ActionType.act_video2.getValue()) {
            ie.putExtra("top", (Boolean) datas[0]);
            ie.putExtra("seekTo", (Integer) datas[1]);
            ie.putExtra("fullScreen", (Boolean) datas[2]);
            if (datas.length > 3 && (Integer) datas[3] > 0) {
            }
            ie.putExtra("adbertOrientation", (Integer) datas[3]);
            if (hideCI)
                ie.putExtra("hideCI", hideCI);
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                new IntentFilter("ad" + ad.pid));

        bundle.putSerializable("videoInfo", ad);
        ie.putExtras(bundle);
        return ie;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action != null) {
                if (action.equals("close")) {
                    int type = intent.getIntExtra("type", 0);
                    int time = intent.getIntExtra("seekTo", 0);
                    boolean returned = intent.getBooleanExtra("returned", false);
                    ad.returned = returned;
                    leavePage = false;
                    if (type == ActionType.act_video2.getValue()) {
                        seekTo = time;
                        ad.goWhere = 1;
                        show();
                    }
                } else if (action.equals("next")) {
                    leavePage = false;
                    if (listener instanceof LoopListener) {
                        ((LoopListener) listener).onCPVEnd();
                    }
                } else if (action.equals("updateSeekTo")) {
                    int time = intent.getIntExtra("seekTo", 0);
                    seekTo = time;
                }
//                else if (action.equals("click") && listener != null && SDKUtil.returnClickEvent) {
//                    listener.onAdClicked();
//                }
            }
        }
    };

    private void openActivity_web(String url) {
        if (destroy) {
        } else {
            Intent ie = getIE(ActionType.act_web.getValue(), url);
            try {
                ie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ie);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
        }
    }

    private void openActivity_video() {
        if (!destroy) {
            int type;
            type = ActionType.act_video2.getValue();
            Intent ie;
            if (!ad.biged) {
                ie = getIE(type, putVideo2AtTop, 0, fullScreen, orientation.getValue());
            } else {
                ie = getIE(type, putVideo2AtTop, video.getCurrentPosition() - 500, fullScreen,
                        orientation.getValue());
            }
            ad.biged = true;
            hideVideoAndRelease();
            videoll.setVisibility(View.GONE);
            try {
                ie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ie);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
        }
    }

    private void cleanCacheFolder() {
        final SharedPreferences settings = context.getSharedPreferences(context.getPackageName(), 0);
        String path = context.getCacheDir().getAbsolutePath() + "/ADBERT";
        cleanFile(20, settings, path + "/others/");
        cleanFile(10, settings, path + "/video/");
    }

    private boolean needToDelete(String lastUseTime) {
        String lastWeekTime = SDKUtil.getLastWeekTime();
        Date lhs = SDKUtil.getDate(lastUseTime);
        Date rhs = SDKUtil.getDate(lastWeekTime);
        if (lhs.getTime() < rhs.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    private void cleanFile(int keepFileCount, SharedPreferences settings, String path) {
        if (new File(path).exists()) {
            String[] list = new File(path).list();
            Map<String, String> map = new HashMap<String, String>();
            ArrayList<String> d = new ArrayList<String>();
            for (int i = 0; i < list.length; i++) {
                String lastTime = settings.getString(path + list[i] + "_useTime", "");
                if (!lastTime.isEmpty()) {
                    if (needToDelete(lastTime)) {
                        new File(path + list[i]).delete();
                    } else {
                        map.put(lastTime, path + list[i]);
                        d.add(lastTime);
                    }
                }
            }
            if (d.size() > 0 && d.size() > keepFileCount) {
                Collections.sort(d, new Comparator<String>() {

                    @Override
                    public int compare(String t1, String t2) {
                        Date lhs = SDKUtil.getDate(t1);
                        Date rhs = SDKUtil.getDate(t2);
                        if (lhs.getTime() < rhs.getTime())
                            return -1;
                        else if (lhs.getTime() == rhs.getTime())
                            return 0;
                        else
                            return 1;
                    }
                });
                for (int i = 0; i < d.size() - keepFileCount; i++) {
                    new File(map.get(d.get(i))).delete();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (size != null) {
            final int width = size.getAdbertWidth();
            final int height = size.getAdbertHeight();
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    private boolean checkStatus() {
        if (size.isLandMode() != size.isScreenPortrait()) {
            return true;
        }
        return false;
    }

    CustomViewListener webListener = new CustomViewListener() {

        public void endingCardAction(int position) {
            AdbertADView.this.endingCardAction(position);
        }

        public void onPageFinished() {

        }

    };
}
