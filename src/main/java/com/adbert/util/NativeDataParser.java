package com.adbert.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.adbert.util.data.CommonNativeAD;
import com.adbert.util.enums.ShareType;

import org.json.JSONException;
import org.json.JSONObject;

public class NativeDataParser {

    private Context context;
    private CommonNativeAD nativeAD = new CommonNativeAD();

    public NativeDataParser(Context context, String dataString, String adType) {
        this.context = context;
        try {
            JSONObject object = new JSONObject(dataString);
            nativeAD.returnUrl = object.getString("returnUrl");
            nativeAD.shareRetrunUrl = object.getString("shareReturnUrl");
            if (object.has("gaUrl"))
                nativeAD.gaUrl = object.getString("gaUrl");
            if (object.has("exposureUrl"))
                nativeAD.gaUrl = object.getString("exposureUrl");
            nativeAD.appId = object.getString("appId");
            nativeAD.appKey = object.getString("appKey");
            nativeAD.pid = object.getString("pid");
            nativeAD.pid = object.getString("pid");
            nativeAD.openBrowser = object.getString("url_open").equals("browser");
            if (adType.equals("native_normal")) {
                parseNative(object);
            } else if (adType.equals("native_video")) {
                parseNativeVideo(object);
            }
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
    }

    private void parseNative(JSONObject object) throws JSONException {
        try {
            if (object.getBoolean("enable_download") && !object.getString("downloadUrl").isEmpty()) {
                nativeAD.shareType = ShareType.download;
                nativeAD.shareContent = object.getString("downloadUrl");
            } else if (object.getBoolean("enable_url") && !object.getString("linkUrl").isEmpty()) {
                nativeAD.shareType = ShareType.url;
                nativeAD.shareContent = object.getString("linkUrl");
            } else if (object.getBoolean("enable_phone") && !object.getString("phone").isEmpty()
                    && SDKUtil.getSimState(context) == TelephonyManager.SIM_STATE_READY) {
                nativeAD.shareType = ShareType.phone;
                nativeAD.shareContent = object.getString("phone");
            } else if (object.getBoolean("enable_fb") && !object.getString("fbUrl").isEmpty()) {
                nativeAD.shareType = ShareType.fb;
                nativeAD.shareContent = object.getString("fbUrl");
                if (object.has("fbPageId")) {
                    nativeAD.fbShortUrl = object.getString("fbPageId");
                }
            } else if (object.getBoolean("enable_line") && !object.getString("lineTxt").isEmpty()
                    && ToolBarAction.checkLineInstalled(context)) {
                nativeAD.shareType = ShareType.line;
                nativeAD.shareContent = object.getString("lineTxt");
            }
            int index = nativeAD.shareType.getIdx();
            if (index >= 0) {
                nativeAD.endingCard[index] = true;
                nativeAD.endingCardText[index] = nativeAD.shareContent;
            }
            nativeAD.publisherData = object.getJSONObject("ad");
        } catch (Exception e) {
            throw e;
        }
    }

    private void parseNativeVideo(JSONObject object) throws JSONException {
        try {
            if (object.getBoolean("enable_download") && !object.getString("downloadUrl").isEmpty()) {
                int position = ShareType.download.getIdx();
                nativeAD.endingCard[position] = true;
                nativeAD.endingCardText[position] = object.getString("downloadUrl");
            }
            if (object.getBoolean("enable_url") && !object.getString("linkUrl").isEmpty()) {
                int position = ShareType.url.getIdx();
                nativeAD.endingCard[position] = true;
                nativeAD.endingCardText[position] = object.getString("linkUrl");
            }
            if (object.getBoolean("enable_phone") && !object.getString("phone").isEmpty()
                    && SDKUtil.getSimState(context) == TelephonyManager.SIM_STATE_READY) {
                int position = ShareType.phone.getIdx();
                nativeAD.endingCard[position] = true;
                nativeAD.endingCardText[position] = object.getString("phone");
            }
            if (object.getBoolean("enable_fb") && !object.getString("fbUrl").isEmpty()) {
                int position = ShareType.fb.getIdx();
                nativeAD.endingCard[position] = true;
                nativeAD.endingCardText[position] = object.getString("fbUrl");
            }
            if (object.getBoolean("enable_line") && !object.getString("lineTxt").isEmpty()
                    && ToolBarAction.checkLineInstalled(context)) {
                int position = ShareType.line.getIdx();
                nativeAD.endingCard[position] = true;
                nativeAD.endingCardText[position] = object.getString("lineTxt");
            }

            JSONObject ad = object.getJSONObject("ad");
            if (ad.has("durationReturnUrl"))
                nativeAD.durationReturnUrl = ad.getString("durationReturnUrl");
            nativeAD.mediaSrc = ad.getString("mediaSrc");
            String tmp = ad.getString("defaultValid");
            if (!tmp.isEmpty()) {
                nativeAD.returnTime = Integer.parseInt(tmp) * 1000;
            }
            nativeAD.absolute = ad.getString("absolute").equals("Y") ? true : false;
        } catch (Exception e) {
            throw e;
        }
    }

    public CommonNativeAD getResult() {
        return nativeAD;
    }

}
