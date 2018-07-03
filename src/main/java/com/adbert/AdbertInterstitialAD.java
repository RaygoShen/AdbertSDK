package com.adbert;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.adbert.util.ConnectionManager;
import com.adbert.util.CustomConnection;
import com.adbert.util.DataParser;
import com.adbert.util.ParamsControl;
import com.adbert.util.SDKUtil;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.LogMsg;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

@SuppressWarnings("ResourceType")
public class AdbertInterstitialAD {

    private Context context;
    private String appId = "", appKey = "", uuId = "";
    private AdbertListener listener;
    private float pWidth;
    private boolean screenPortrait;
    private String jsonStr = "";
    private boolean ready = false;
    private boolean forDemo = false;
    private String pageInfo = "";
    private boolean isTestMode = false;

    public void setTestMode() {
        isTestMode = true;
    }

    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    public String getVersion() {
        return SDKUtil.version;
    }

    boolean hideCI = false;

    public void hideCI() {
        hideCI = true;
    }

    public AdbertInterstitialAD(Context context) {
        SDKUtil.logInfo_inters(AdbertParams.Version.toString());
        this.context = context;

        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            pWidth = dm.widthPixels;
        } catch (Exception e) {
            SDKUtil.logException(e);

            try {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(size);
                }
                pWidth = size.x;
            } catch (Exception e2) {
                SDKUtil.logException(e2);
            }
        }
        screenPortrait = SDKUtil.isPortrait(context);
        SDKUtil.initCookie(context);
    }

    public void setListener(AdbertListener listener) {
        this.listener = listener;
    }

    public void setAPPID(String appId, String appKey) {
        this.appId = appId.trim();
        this.appKey = appKey.trim();
    }

    boolean isMediation = false;

    public void setMediationAPPID(String serverParameter) {
        isMediation = true;
        if (serverParameter.contains("|")) {
            this.appId = serverParameter.substring(0, serverParameter.indexOf("|"));
            this.appKey = serverParameter.substring(serverParameter.indexOf("|") + 1);
        }
    }

    boolean destroy = false;

    public void destroy() {
        destroy = true;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action != null && listener != null) {
                if (action.equals("close")) {
                    listener.onAdClosed();
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
                }
//                else if (action.equals("click") && SDKUtil.returnClickEvent) {
//                    listener.onAdClicked();
//                }
            }
        }
    };

    public void show() {
        if (ready) {
            if (!destroy) {
                if (interstitial != null) {
                    if (interstitial.isLoaded()) {
                        interstitial.show();
                    }
                } else {
                    Intent ie = new Intent(context.getApplicationContext(), AdbertInterstitialActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("jsonStr", jsonStr);
                    bundle.putBoolean("hideCI", hideCI);
                    ie.putExtras(bundle);
                    LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                            new IntentFilter("ad" + pid));
                    try {
                        ie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(ie);
                    } catch (Exception e) {
                        SDKUtil.logException(e);
                    }
                }
            }
        } else {
            returnFail(LogMsg.NOT_READY.getValue());
        }
    }

    InterstitialAd interstitial;

    public void loadAd() {
        if (!SDKUtil.connectable(context)) {
            returnFail(LogMsg.ERROR_CONNECTION.getValue());
        } else if (appId.isEmpty() || appKey.isEmpty()) {
            returnFail(LogMsg.ERROR_ID_NULL.getValue());
        } else {
            SDKUtil.logInfo_inters(LogMsg.START.getValue());
            SDKUtil.getUUID(context, new SDKUtil.GetUUIDListener() {
                @Override
                public void onResult(String result) {
                    uuId = result;
                    ConnectionManager.getInstance().newSimpleConnection(context, AdbertParams.ADURL.getHashURL(uuId),
                            getRequestParam(), new ConnectionManager.SimpleConnectionListener() {
                                @Override
                                public void onEnd(int code, String result) {
                                    parseData(code, result);
                                }
                            });
                }
            });
        }
    }

    private String getRequestParam() {
        ParamsControl paramsControl = new ParamsControl(context);
        String testMode = "";
        if (isTestMode) testMode = "&testMode=1";
        String normalParamas = paramsControl.getNormalParams() + testMode;
        String paramStr = paramsControl.getCPMParams(appId, appKey, uuId,
                AdbertParams.INSERT_AD.toString(), screenPortrait, pageInfo)
                + normalParamas;
        return paramStr;
    }

    private void parseData(int responseCode, String jsonStr) {
        if (SDKUtil.isAllus() && (responseCode != 200 || jsonStr.isEmpty())) {
            setInterstitial();
        } else if (responseCode == 200) {
            if (jsonStr.isEmpty()) {
                returnFail(LogMsg.ERROR_JSON_EMPTY_INTERS.getValue());
            } else {
                setDatas(jsonStr);
            }
        } else {
            returnFail(LogMsg.ERROR_SERVICE.getValue());
        }
    }

    private void returnFail(String message) {
        SDKUtil.logWarning_inters(message);
        listener.onFailedReceive(message);
    }

    private void returnSuccess(String message) {
        SDKUtil.logInfo_inters(message);
        listener.onReceive(message);
    }

    private void setInterstitial() {
        interstitial = new InterstitialAd(context);
        interstitial.setAdUnitId(AdbertParams.intersID.getValue());
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                setReady();
            }

            public void onAdFailedToLoad(int errorCode) {
                returnFail(LogMsg.ERROR_JSON_EMPTY_INTERS.getValue());
            }
        });
    }

    private boolean checkResourceString(String... urls) {
        for (int i = 0; i < urls.length; i++) {
            if (urls[i] == null || urls[i].isEmpty() || urls[i].endsWith("/")) {
                return false;
            }
        }
        return true;
    }

    private String pid = "";

    private void setDatas(String jsonStr) {
        this.jsonStr = jsonStr;
        try {
            CommonData adInfo = new DataParser(context).parse(jsonStr, screenPortrait);
            pid = adInfo.pid;
            if (adInfo.type == AdbertADType.cpm_web) {
                setReady();
            } else if (adInfo.type == AdbertADType.cpm_banner) {
                String imgUrl = adInfo.cpmBannerImg;
                if (checkResourceString(imgUrl)) {
                    if (SDKUtil.isGIF(imgUrl)) {
                        setReady();
                    } else {
                        getImage_cpmBanner(adInfo.cpmBannerImg);
                    }
                } else {
                    returnFail(LogMsg.ERROR_RESOURCE_FORMAT.getValue());
                }
            } else if (adInfo.type == AdbertADType.cpm_video) {
                if (SDKUtil.isSDKunder14()) {
                    returnFail(LogMsg.VIDEO_NOT_SUPPORT.getValue());
                } else {
                    if (checkResourceString(adInfo.mediaSource, adInfo.mediaSourceSmall)) {
                        downloadFiles_cpmVideo(adInfo.adServing, adInfo.mediaSource, adInfo.mediaSourceSmall);
                    } else {
                        returnFail(LogMsg.ERROR_RESOURCE_FORMAT.getValue());
                    }
                }
            }

//            JSONObject ad = new JSONObject(jsonStr);
//            pid = ad.getString(AdbertKey.pid.toString());
//            if (ad.getString(AdbertKey.mediaType.toString()) == null
//                    || ad.getString(AdbertKey.mediaType.toString()).isEmpty()) {
//                returnFail(LogMsg.ERROR_TYPE_NULL.getValue());
//            }
//            boolean adServing = false;
//            if (ad.has("adServing")) {
//                adServing = ad.getBoolean("adServing");
//            }
//            if ((ad.has(AdbertKey.defaultCreative.toString())
//                    && !ad.getString(AdbertKey.defaultCreative.toString()).isEmpty() && Integer.parseInt(ad
//                    .getString(AdbertKey.defaultCreative.toString())) > 0)) {
//                setReady();
//            } else if (ad.getString(AdbertKey.mediaType.toString())
//                    .equals(AdbertADType.cpm_banner.toString())) {
//                final String pic = ad.getString(AdbertKey.fillbannerUrl_P.toString());
//                final String picL = ad.getString(AdbertKey.fillbannerUrl_L.toString());
//                if (adServing) {
//                    setReady();
//                } else if (screenPortrait && checkResourceString(pic)) {
//                    if (SDKUtil.isGIF(pic)) {
//                        setReady();
//                    } else {
//                        getImage_cpmBanner(pic);
//                    }
//                } else if (!screenPortrait && checkResourceString(picL)) {
//                    if (SDKUtil.isGIF(picL)) {
//                        setReady();
//                    } else {
//                        getImage_cpmBanner(picL);
//                    }
//                } else {
//                    returnFail(LogMsg.ERROR_RESOURCE_FORMAT.getValue());
//                }
//            } else if (ad.getString(AdbertKey.mediaType.toString()).equals(AdbertADType.cpm_video.toString())) {
//                if (SDKUtil.isSDKunder14()) {
//                    returnFail(LogMsg.VIDEO_NOT_SUPPORT.getValue());
//                } else {
//                    String media = ad.getString(AdbertKey.mediaSrc.toString());
//                    String mediaSmall = ad.getString(AdbertKey.fillbannerUrl.toString());
//                    if (checkResourceString(media, mediaSmall)) {
//                        downloadFiles_cpmVideo(adServing, media, mediaSmall);
//                    } else {
//                        returnFail(LogMsg.ERROR_RESOURCE_FORMAT.getValue());
//                    }
//                }
//            }
        } catch (Exception e) {
            SDKUtil.logException(e);
            returnFail(LogMsg.ERROR_JSON_PARSE.getValue() + e.getMessage());
        }
    }

    private void getImage_cpmBanner(final String url) {
        ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
            @Override
            public void onConnectionSuccess(CustomConnection cc) {
                if (!destroy) {
                    try {
                        String savePath = SDKUtil.getFileNameFromUrl(context, cc.getFinalUrl());
                        boolean b = SDKUtil.savePic(cc.getBitmap(), url, cc.getFinalUrl(), savePath);
                        if (b) {
                            setReady();
                        } else {
                            returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
                        }
                        cc.recycleBitmap();
                    } catch (Exception e) {
                        SDKUtil.logException(e);
                        returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
                    }
                }
            }

            @Override
            public void onConnectionFail(CustomConnection cc) {
                returnFail(LogMsg.ERROR_BITMAP_NULL.getValue());
            }
        }).getImage(url);
    }

    private void downloadFiles_cpmVideo(boolean adServing, final String mediaURL, final String imageURL) {
        if (adServing) {
            ConnectionManager.ConnectionListener listener = new ConnectionManager.ConnectionListener() {
                @Override
                public void onConnectionSuccess(CustomConnection cc) {
                    setReady();
                }

                @Override
                public void onConnectionFail(CustomConnection cc) {
                    returnFail(LogMsg.ERROR_DOWNLOAD_FILE.getValue());
                }
            };
            ConnectionManager cm = ConnectionManager.getInstance();
            String savePath = SDKUtil.getFileNameFromUrl(context, mediaURL);
            cm.newConnection(context).setListener(listener).setLoaderId(0).getFileAndSave(mediaURL, savePath);
        }  else {
            ConnectionManager.ConnectionListener listener = new ConnectionManager.ConnectionListener() {
                @Override
                public void onConnectionSuccess(CustomConnection cc) {
                    if (!destroy) {
                        try {
                            if (cc.getLoaderId() == 1) {
                                String savePath = SDKUtil.getFileNameFromUrl(context, cc.getFinalUrl());
                                boolean b = SDKUtil.savePic(cc.getBitmap(), imageURL, cc.getFinalUrl(), savePath);
                                if (b) {
                                    successCount++;
                                }
                                cc.recycleBitmap();
                            } else {
                                successCount++;
                            }
                            resultCount++;
                            end();
                        } catch (Exception e) {
                            SDKUtil.logException(e);
                        }
                    }
                }

                @Override
                public void onConnectionFail(CustomConnection cc) {
                    resultCount++;
                    end();
                }

                int resultCount = 0;
                int successCount = 0;

                private void end() {
                    if (resultCount == 2) {
                        if (successCount == 2) {
                            setReady();
                        } else {
                            returnFail(LogMsg.ERROR_DOWNLOAD_FILE.getValue());
                        }
                    }
                }
            };
            ConnectionManager cm = ConnectionManager.getInstance();
            String savePath = SDKUtil.getFileNameFromUrl(context, mediaURL);
            cm.newConnection(context).setListener(listener).setLoaderId(0).getFileAndSave(mediaURL, savePath);
            cm.newConnection(context).setListener(listener).setLoaderId(1).getImage(imageURL);
        }
    }

    public void setReady() {
        ready = true;
        returnSuccess(LogMsg.READY.getValue());
    }
}
