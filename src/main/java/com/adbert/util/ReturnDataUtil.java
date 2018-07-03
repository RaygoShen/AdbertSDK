package com.adbert.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.adbert.util.data.CommonData;

/**
 * Created by chihhan on 2017/9/22.
 */

public class ReturnDataUtil {

    public static void returnEvent(final Context context, final CommonData videoInfo,
                                   final Runnable runnableWhenFail) {
        final String paramsStr = new ParamsControl(context).getReturnParams(videoInfo.appId,
                videoInfo.appKey, videoInfo.uuId, videoInfo.pid);
        ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
            @Override
            public void onConnectionSuccess(CustomConnection customConnection) {
            }

            @Override
            public void onConnectionFail(CustomConnection customConnection) {
                if (runnableWhenFail != null) {
                    runnableWhenFail.run();
                }
            }
        }).post(videoInfo.returnUrl, paramsStr);
    }

    public static void shareReturn(final Context context, final CommonData videoInfo,
                                   final String shareType) {
        String mediaType = getMediaType(videoInfo);
        final String paramsStr = new ParamsControl(context).getShareParams(videoInfo.appId,
                videoInfo.appKey, videoInfo.uuId, shareType, videoInfo.pid, mediaType);
        ConnectionManager.getInstance().newConnection(context).post(videoInfo.shareReturnUrl, paramsStr);
    }

    public static void actionReturn(final Context context, final CommonData videoInfo,
                                    final String actionType) {
        final String paramsStr = new ParamsControl(context).getActionParams(videoInfo.appId,
                videoInfo.appKey, videoInfo.uuId, videoInfo.pid, actionType);
        ConnectionManager.getInstance().newConnection(context).post(videoInfo.actionReturnUrl, paramsStr);
    }

    public static void exposureEvent(final Context context, final CommonData videoInfo, final Runnable runnableWhenFail) {
        if (!videoInfo.exposureUrl.isEmpty()) {
            String mediaType = getMediaType(videoInfo);
            final String paramsStr = new ParamsControl(context).getExposureParams(videoInfo.appId,
                    videoInfo.appKey, videoInfo.pid, mediaType, videoInfo.uuId);
            ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
                @Override
                public void onConnectionSuccess(CustomConnection cc) {

                }

                @Override
                public void onConnectionFail(CustomConnection cc) {
                    if (runnableWhenFail != null) {
                        runnableWhenFail.run();
                    }
                }
            }).post(videoInfo.exposureUrl, paramsStr);
        }
    }

    public static void exposureEvent(final Context context, final CommonData videoInfo) {
        exposureEvent(context, videoInfo, null);
    }

    public static void iBeaconReturn(final Context context, String result, final CommonData data) {
        final String param = "result=" + result;
        if (!data.iBeaconsUrl.isEmpty()) {
            ConnectionManager.getInstance().newConnection(context).post(data.iBeaconsUrl, param);
        }
    }

    private static String getMediaType(CommonData videoInfo) {
        boolean b = videoInfo.realType == null;
        String mediaType = b ? videoInfo.type.toString() : videoInfo.realType.toString();
        return mediaType;
    }

    public static void exposureEvent_forUnderV14Video(final Context context, final CommonData videoInfo,
                                                      final Runnable runnable) {
        String mediaType = getMediaType(videoInfo);
        final String paramsStr = new ParamsControl(context).getExposureParams(videoInfo.appId,
                videoInfo.appKey, videoInfo.pid, mediaType, videoInfo.uuId);

        ConnectionManager.getInstance().newConnection(context).setListener(new ConnectionManager.ConnectionListener() {
            @Override
            public void onConnectionSuccess(CustomConnection customConnection) {
                end();
            }

            @Override
            public void onConnectionFail(CustomConnection customConnection) {
                end();
            }

            private void end() {
                runnable.run();
            }
        }).post(videoInfo.exposureUrl, paramsStr);
    }

    public static void durationReturn(final Context context, final CommonData videoInfo,
                                      int seconds) {
        final String paramsStr = new ParamsControl(context).getSecondsParams(videoInfo.appId,
                videoInfo.appKey, videoInfo.uuId, videoInfo.pid, seconds);
        ConnectionManager.getInstance().newConnection(context).post(videoInfo.durationReturnUrl, paramsStr);

    }

    public static boolean connectable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
