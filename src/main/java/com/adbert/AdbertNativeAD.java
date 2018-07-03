package com.adbert;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.adbert.util.ConnectionManager;
import com.adbert.util.NativeDataParser;
import com.adbert.util.ParamsControl;
import com.adbert.util.SDKUtil;
import com.adbert.util.ToolBarAction;
import com.adbert.util.data.CommonNativeAD;
import com.adbert.util.enums.ActionType;
import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.LogMsg;
import com.adbert.view.TrackingView;

import org.json.JSONObject;

import java.util.List;

public class AdbertNativeAD {

    private Context context;
    private String appId = "", appKey = "", uuId = "", pageInfo = "";
    private AdbertNativeADListener listener;
    private boolean isReady = false;
    private CommonNativeAD nativeAD;
    private boolean isTestMode = false;

    public void setTestMode() {
        isTestMode = true;
    }

    public AdbertNativeAD(final Context context, String appId, String appKey) {
        this.context = context;
        this.appId = appId.trim();
        this.appKey = appKey.trim();
    }

    public void setListener(AdbertNativeADListener listener) {
        this.listener = listener;
    }

    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    public void loadAD() {
        if (appId.isEmpty() || appKey.isEmpty()) {
            returnFail(LogMsg.ERROR_ID_NULL.getValue());
        } else if (SDKUtil.connectable(context)) {
            // get uuid first
            SDKUtil.getUUID(context, new SDKUtil.GetUUIDListener() {
                @Override
                public void onResult(String result) {
                    uuId = result;
                    request();
                }
            });
        } else
            returnFail(LogMsg.ERROR_CONNECTION.getValue());
    }

    public boolean isReady() {
        return isReady;
    }

    public JSONObject getData() {
        if (nativeAD != null)
            return nativeAD.publisherData;
        return new JSONObject();
    }

    private void request() {
        String testMode = "";
        if (isTestMode) testMode = "&testMode=1";
        final String paramsStr = new ParamsControl(context).getNativeADParams(appId, appKey, uuId, "",
                pageInfo) + testMode;
        ConnectionManager.getInstance().newSimpleConnection(context, AdbertParams.nativeADURL.getHashURL(uuId), paramsStr, new ConnectionManager.SimpleConnectionListener() {
            @Override
            public void onEnd(int code, String result) {
                if (code != 200) {
                    returnFail(LogMsg.ERROR_SERVICE.getValue());
                } else if (result != null && result.isEmpty()) {
                    returnFail(LogMsg.ERROR_JSON_EMPTY.getValue());
                } else if (result != null && !result.isEmpty()) {
                    nativeAD = new NativeDataParser(context, result, "native_normal").getResult();
                    if (!nativeAD.gaUrl.isEmpty() && view != null) {
                        setTrackingView(view);
                    }
                    isReady = true;
                    returnSuccess("Success");
                }
            }
        });
    }

    @SuppressWarnings("ResourceType")
    public void registerView(View view) {
        setView(view);

    }
    @SuppressWarnings("ResourceType")
    public void unregisterView(View view) {
        if (view != null) {
            view.setClickable(false);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });


        }
    }

    void click() {
        if (isReady) {
            int type = nativeAD.shareType.getIdx();
            if (type >= 0) {
                returnClick();
                returnShare();
//                if(listener!=null && SDKUtil.returnClickEvent){
//                    listener.onAdClicked();
//                }
                ToolBarAction.getToolBar(context).toolbarAction(nativeAD.getCommonAD(), type, null);
            }
        }
    }

    private void returnClick() {
        if (!nativeAD.returned && !nativeAD.returnUrl.isEmpty()) {
            nativeAD.returned = true;
            final String paramsStr = new ParamsControl(context).getReturnParams(nativeAD.appId,
                    nativeAD.appKey, uuId, nativeAD.pid);
            ConnectionManager.getInstance().newSimpleConnection(context, nativeAD.returnUrl, paramsStr, new ConnectionManager.SimpleConnectionListener() {
                @Override
                public void onEnd(int code, String result) {
                    nativeAD.returned = (code == 200);
                }
            });
        }
    }

    private void returnShare() {
        String paramsStr = new ParamsControl(context).getShareParams(nativeAD.appId,
                nativeAD.appKey, uuId, nativeAD.shareType.toString(), nativeAD.pid, "");
        ConnectionManager.getInstance().newSimpleConnection(context, nativeAD.shareRetrunUrl, paramsStr);
    }

    private void returnSuccess(String msg) {
        if (listener != null)
            listener.onReceived(msg);
    }

    private void returnFail(String msg) {
        if (listener != null)
            listener.onFailReceived(msg);
    }

    private void openActivity_web(String url) {
        Intent ie = new Intent(context.getApplicationContext(), AdbertActivity.class);
        Bundle bundle = new Bundle();
        ie.putExtra("datas",
                new Object[]{uuId, !SDKUtil.isPortrait(context), context.getPackageName(), ActionType.act_web.getValue()});
        ie.putExtra("url", url);
        // inapp用不到，傳null就好
        bundle.putSerializable("videoInfo", null);
        ie.putExtras(bundle);
        try {
            ie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ie);
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
    }

    View view = null;

    @SuppressWarnings("ResourceType")
    private void setView(View view) {
        this.view = view;
        view.setClickable(true);
        setViewEvent(view);
//        getViewVisible(view);

//        getPackage();


    }

    private boolean tracked = false;

    private void setTrackingView(View view) {
        if (!tracked && context != null) {
            ViewGroup vg;
            try {
                vg = (ViewGroup) view;
                TrackingView trackingView = new TrackingView(context);
                vg.addView(trackingView);
                trackingView.loadUrl(nativeAD.gaUrl);
                tracked = true;
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
        }
    }

    private void setViewEvent(View view) {

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            private long mDeBounce = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (Math.abs(mDeBounce - motionEvent.getEventTime()) < 250) {
                    return true;
                }

                int intCurrentY = Math.round(motionEvent.getY());
                int intCurrentX = Math.round(motionEvent.getX());
                int intStartY = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalY(0)) : intCurrentY;
                int intStartX = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalX(0)) : intCurrentX;

                if ((motionEvent.getAction() == MotionEvent.ACTION_UP) && (Math.abs(intCurrentX - intStartX) < 3) && (Math.abs(intCurrentY - intStartY) < 3)) {
                    if (mDeBounce > motionEvent.getDownTime()) {

                        return true;
                    }
                    click();

                    mDeBounce = motionEvent.getEventTime();

                    return true;
                }
                return false;
            }
        });
        if (isReady && !nativeAD.gaUrl.isEmpty() && view != null) {
            setTrackingView(view);
        }
    }

    private void getViewVisible(View view){

        if(view.getWindowVisibility() == View.VISIBLE){
            //view visible
//            Toast.makeText(this.context, " ON VISIBLE", Toast.LENGTH_SHORT).show();

        }

    }

    private void getPackage(){
        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(0);

//        ArrayList<AppInfo> res = new ArrayList<AppInfo>();
        for(int i=0;i<apps.size();i++) {
            PackageInfo p = apps.get(i);
            if(p.packageName.startsWith("com.google.android")){//filter out default package name
            }

        }

    }

}
