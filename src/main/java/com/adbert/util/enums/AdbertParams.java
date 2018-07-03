package com.adbert.util.enums;

import com.adbert.util.SDKUtil;
import com.adbert.util.TestMode;

public enum AdbertParams {
    appId, appKey, mediaType, pid, responseCode, responseStr, Version, vibrate, album, camera, shake,
    distance, adbertcamera, adbertalbum, adbertbrowser, goWhere, COMMON_AD, INSERT_AD,
    SDKVersion, permission, orientation, sharetype, uuid, APPID,
    APPKEY, UUID, AD_MODE, ADURL("sdk_api_v2/auth/"), is("="), and("&"),
    FIRST_REQUEST("APP_REQUEST=Y"), pageInfo, pageInfo_inters, actiontype, build, lightSensor, device,
    macAddress, OSVersion, GPS, timestamp, operatorName, connectType, seconds,
    Infos, reciprocal("5YCS5pW456eS"), host("https://staging-dsp.adbert.com.tw/portal/"),
    country, language, bannerID("ca-app-pub-1993641140901979/6895071609"),
    intersID("ca-app-pub-1993641140901979/2928616873"), nativeADURL("sdk_api_v3/auth/"), packageName;

    /* old :
     bannerID("ca-app-pub-5088425180059605/6170128179"),
     intersID("ca-app-pub-5088425180059605/9123594577")

     stage url :
     host("http://staging-dsp.adbert.com.tw/portal/")
     */
    private String code = "";

    AdbertParams() {
        this.code = this.toString();
    }

    AdbertParams(String code) {
        this.code = code;
    }

    public String getValue() {
        if (SDKUtil.log && !TestMode.testUrl.isEmpty() && this == host) {
            if (TestMode.testUrl.endsWith("/"))
                return TestMode.testUrl;
            else return TestMode.testUrl + "/";
        }
        if (this == ADURL || this == nativeADURL) {
            return host.getValue() + this.code;
        }
        return code;
    }

    public String getHashURL(String uuid) { //just for ADURL and nativeADURL
        String url = host.getValue() + this.code;
        try {
            url += "?ac=" + SDKUtil.bin2hex(uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public String toString() {
        if (this == Version)
            return super.toString() + " : " + SDKUtil.version;
        else if (this == adbertcamera || this == adbertalbum || this == adbertbrowser) {
            return super.toString() + " : ";
        } else
            return super.toString();
    }

    public int length() {
        return this.toString().length();
    }

    public String setValue(String str) {
        return this.toString() + AdbertParams.is.getValue() + str;
    }

    public String AndSetValue(String str) {
        return AdbertParams.and.getValue() + this.toString() + AdbertParams.is.getValue() + str;
    }
}
