package com.adbert.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.adbert.util.enums.AdbertParams;
import com.adbert.util.enums.SensorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ResourceType")
public class ParamsControl {

    private Context context;
    private String version = "";
    private String build = "";
    private String device = "";
    private String macAddress = "";
    private String OSVersion = "";
    private String gps = "";
    private String timestamp = "";
    private String operatorName = "";
    private String connectType = "";
    private String language = "";
    private String country = "";
    private SharedPreferences settings;

    public ParamsControl(Context context) {
        this.context = context;
        this.version = SDKUtil.version;
        this.build = String.valueOf(SDKUtil.build);
        settings = context.getSharedPreferences(AdbertParams.Infos.getValue(), 0);
        getLocation();
        this.device = Build.DEVICE + "/" + Build.MODEL;
        if (device.contains(" "))
            device.replace(" ", "_");
        this.OSVersion = Build.VERSION.RELEASE + "(" + Build.VERSION.SDK_INT + ")";
        try {
            this.macAddress = getMacAddress();
            settings.edit().putString(AdbertParams.macAddress.getValue(), this.macAddress).commit();
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        this.timestamp = SDKUtil.getTime();
        try {
            this.connectType = getConnnectType();
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        try {
            this.operatorName = getOperatorName();
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        try {
            this.language = Locale.getDefault().getLanguage();
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        try {
            this.country = Locale.getDefault().getCountry();
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
    }

    public String getNativeADParams(String appId, String appKey, String uuId, String adType, String pageInfo) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.pageInfo.AndSetValue(pageInfo);
        paramsStr += AdbertParams.uuid.AndSetValue(uuId);
        paramsStr += "&AD_TYPE=" + adType;
        paramsStr += getNormalParams();
        return paramsStr;
    }

    // for banner request, need to post firstRequest
    public String getBannerParams(String appId, String appKey, String uuId, String adMode,
                                  boolean screenPortrait, boolean firstRequestData, String pageInfo) {
        if (uuId == null) {
            uuId = "";
        }
        settings.edit().putString(AdbertParams.uuid.getValue(), uuId).commit();
        String paramsStr = "";
        paramsStr = AdbertParams.APPID.setValue(appId) + AdbertParams.APPKEY.AndSetValue(appKey)
                + AdbertParams.UUID.AndSetValue(uuId) + AdbertParams.AD_MODE.AndSetValue(adMode);
        if (!pageInfo.isEmpty()) {
            paramsStr += AdbertParams.pageInfo.AndSetValue(pageInfo);
            settings.edit().putString(AdbertParams.pageInfo.getValue(), pageInfo).commit();
        }
        if (firstRequestData)
            paramsStr += AdbertParams.and.getValue() + AdbertParams.FIRST_REQUEST.getValue();
        return paramsStr;
    }

    public String getCPMParams(String appId, String appKey, String uuId, String adMode,
                               boolean screenPortrait, String pageInfo) {
        if (uuId == null) {
            uuId = "";
        }
        settings.edit().putString(AdbertParams.uuid.getValue(), uuId).commit();
        String paramsStr = "";
        String orientation = screenPortrait ? "0" : "1";
        paramsStr = AdbertParams.APPID.setValue(appId) + AdbertParams.APPKEY.AndSetValue(appKey)
                + AdbertParams.UUID.AndSetValue(uuId) + AdbertParams.AD_MODE.AndSetValue(adMode)
                + AdbertParams.orientation.AndSetValue(orientation) + getPermission();
        if (!pageInfo.isEmpty()) {
            paramsStr += AdbertParams.pageInfo.AndSetValue(pageInfo);
            settings.edit().putString(AdbertParams.pageInfo_inters.getValue(), pageInfo).commit();
        }
        return paramsStr;
    }

    public String getShareParams(String appId, String appKey, String uuId, String shareType, String pid,
                                 String mediaType) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.uuid.AndSetValue(uuId) + AdbertParams.sharetype.AndSetValue(shareType)
                + AdbertParams.pid.AndSetValue(pid) + getNormalParams();
        if (!mediaType.isEmpty())
            paramsStr += AdbertParams.mediaType.AndSetValue(mediaType);
        return paramsStr;
    }

    public String getActionParams(String appId, String appKey, String uuId, String pid, String actionType) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.uuid.AndSetValue(uuId) + AdbertParams.actiontype.AndSetValue(actionType)
                + AdbertParams.pid.AndSetValue(pid) + getNormalParams();
        return paramsStr;
    }

    public String getReturnParams(String appId, String appKey, String uuId, String pid) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.uuid.AndSetValue(uuId) + AdbertParams.pid.AndSetValue(pid) + getNormalParams();
        return paramsStr;
    }

    public String getExposureParams(String appId, String appKey, String pid, String mediaType, String uuId) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.pid.AndSetValue(pid) + AdbertParams.uuid.AndSetValue(uuId)
                + AdbertParams.mediaType.AndSetValue(mediaType) + getNormalParams();
        return paramsStr;
    }

    public String getSecondsParams(String appId, String appKey, String uuId, String pid, int seconds) {
        if (uuId == null) {
            uuId = "";
        }
        String paramsStr = "";
        paramsStr = AdbertParams.appId.setValue(appId) + AdbertParams.appKey.AndSetValue(appKey)
                + AdbertParams.pid.AndSetValue(pid) + AdbertParams.uuid.AndSetValue(uuId)
                + AdbertParams.seconds.AndSetValue(String.valueOf(seconds)) + getNormalParams();
        return paramsStr;
    }

    public String getNormalParams() {
        // default start with &
        String paramsStr = "";
        paramsStr = AdbertParams.SDKVersion.AndSetValue(SDKUtil.version)
                + AdbertParams.build.AndSetValue(String.valueOf(SDKUtil.build))
                + AdbertParams.device.AndSetValue(device) + AdbertParams.macAddress.AndSetValue(macAddress)
                + AdbertParams.OSVersion.AndSetValue(OSVersion) + AdbertParams.GPS.AndSetValue(gps)
                + AdbertParams.timestamp.AndSetValue(timestamp)
                + AdbertParams.operatorName.AndSetValue(operatorName)
                + AdbertParams.connectType.AndSetValue(connectType)
                + getPackageName()
                + AdbertParams.country.AndSetValue(country) + AdbertParams.language.AndSetValue(language);
        paramsStr += "&screenSize=" + new ScreenSize(context).getScreenSize();

        return paramsStr;
    }

    private String getPackageName() {
        try {
            return AdbertParams.packageName.AndSetValue(context.getPackageName());
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        return AdbertParams.packageName.AndSetValue("");
    }

    private String getOperatorName() throws Exception {
        if (SDKUtil.checkPermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager telManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return telManager.getNetworkOperatorName();
        } else
            return "";
    }

    private String getConnnectType() throws Exception {
        String type = "";
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // For 3G check
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        // For WiFi Check
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if (is3g) {
            if (getNetworkClass(context).isEmpty()){
                type += "3G";
            }else{
                type += getNetworkClass(context);
            }
        }
        if (isWifi) {
            if (!type.isEmpty())
                type += "/";
            type += "wifi";
        }
        return type;
    }

    private String getNetworkClass(Context context) {
        if (SDKUtil.checkPermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager mTelephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = mTelephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                default:
                    return "";
            }
        }
        return "";
    }

    private void getLocation() {
        settings.edit().putString("LastLocation", settings.getString("Location", "")).commit();
        settings.edit().remove("Location").commit();

        if (SDKUtil.checkPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                || SDKUtil.checkPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new GetLocation(context, new GetLocation.GetLocationListener() {

                @Override
                public void onSuccess(double lat, double lon) {
                    ParamsControl.this.gps = lat + "," + lon;
                    settings.edit().putString("Location", ParamsControl.this.gps).commit();
                }

                @Override
                public void onFail() {
                    ParamsControl.this.gps = "";
                }

            });
        }
    }

    private String getPermission() {
        List<String> permissions = new ArrayList<String>();
        if (SDKUtil.checkPermission(context, android.Manifest.permission.VIBRATE)) {
            permissions.add(AdbertParams.vibrate.toString());
        }
        if (SDKUtil.checkPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissions.add(AdbertParams.album.toString());
        }
        if (SDKUtil.checkPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && SDKUtil.checkPermission(context, android.Manifest.permission.CAMERA)) {
            permissions.add(AdbertParams.camera.toString());
        }
        //
        SensorMode sensorMode = new SensorMode(context);
        if (sensorMode.isSupported(SensorType.SHAKE)) {
            permissions.add(AdbertParams.shake.toString());
        }
        if (sensorMode.isSupported(SensorType.DISTANCE)) {
            permissions.add(AdbertParams.distance.toString());
        }
        if (sensorMode.isSupported(SensorType.DISTANCE)) {
            permissions.add(AdbertParams.lightSensor.toString());
        }
        String permissionStr = "";
        for (int i = 0; i < permissions.size(); i++) {
            if (i > 0)
                permissionStr += ",";
            permissionStr += permissions.get(i);
        }
        permissionStr = AdbertParams.permission.AndSetValue(permissionStr);
        return permissionStr;
    }

    WifiManager wifiMan;
    WifiInfo wifiInf;

    // android.permission.ACCESS_WIFI_STATE
    private String getMacAddress() throws Exception {
        if (Build.VERSION.SDK_INT < 23
                && SDKUtil.checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
            if (wifiMan == null)
                wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiInf == null)
                wifiInf = wifiMan.getConnectionInfo();
            if (wifiInf != null)
                return wifiInf.getMacAddress();
            else
                return "";
        } else
            return "";
    }
}
