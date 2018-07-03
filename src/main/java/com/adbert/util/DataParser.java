package com.adbert.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.enums.JSONKey;
import com.adbert.util.enums.ShareType;

import org.json.JSONArray;
import org.json.JSONException;

public class DataParser {

    Context context;

    public DataParser(Context context) {
        this.context = context;
    }

    public CommonData parse(String jsonStr, boolean isScreenPortrait) throws Exception {
        CommonData adInfo = new CommonData();
        try {
            MyJSONObject json = new MyJSONObject(jsonStr);
            adInfo.gaUrl = json.getString(JSONKey.gaUrl);
            adInfo.returnUrl = json.getString(JSONKey.returnUrl);
            adInfo.appId = json.getString(JSONKey.appId);
            adInfo.appKey = json.getString(JSONKey.appKey);
            adInfo.shareReturnUrl = json.getString(JSONKey.shareReturnUrl);
            adInfo.exposureUrl = json.getString(JSONKey.exposureUrl);
            adInfo.actionReturnUrl = json.getString(JSONKey.actionReturnUrl);
            adInfo.durationReturnUrl = json.getString(JSONKey.durationReturnUrl);
            adInfo.shouldOpen = json.getString(JSONKey.IniExpand).equals("Y");
            try {
                String valid = json.getString(JSONKey.defaultValid);
                adInfo.returnTime = Integer.parseInt(valid) * 1000;
            } catch (Exception e) {
                adInfo.returnTime = 0;
            }
            adInfo.pid = json.getString(JSONKey.pid);
            adInfo.special = adInfo.pid.equals("0");
            adInfo.isFullScreen = json.getBoolean(JSONKey.isFullScreen);
            adInfo.absolute = json.getString(JSONKey.absolute).equals("Y");
            adInfo.url_openInAPP = json.getString(JSONKey.url_open).equals("inapp");
            adInfo.adServing = json.getBoolean(JSONKey.adServing);

            //set type
            String mediaType = json.getString(JSONKey.mediaType);
            int defaultCreative = 0;
            try {
                defaultCreative = Integer.parseInt(json.getString(JSONKey.defaultCreative));
            } catch (Exception e) {
                defaultCreative = 0;
            }
            AdbertADType realType = AdbertADType.banner.getTypeFromStr(mediaType);
            if (realType != null) {
                adInfo.setMediaType(realType, defaultCreative);
            }

            //set image path and video path
            switch (adInfo.type) {
                case video:
                    adInfo.mediaSource = json.getString(JSONKey.mediaSrc);
                    adInfo.mediaSourceSmall = json.getString(JSONKey.mediaSrc_S);
                case banner:
                    adInfo.banner = json.getString(JSONKey.bannerUrl);
                case cpm_banner:
                    JSONKey key = isScreenPortrait ? JSONKey.fillbannerUrl_P : JSONKey.fillbannerUrl_L;
                    adInfo.cpmBannerImg = json.getString(key);
                case cpm_video:
                    adInfo.mediaSource = json.getString(JSONKey.mediaSrc);
                    adInfo.mediaSourceSmall = json.getString(JSONKey.fillbannerUrl);
                default:
                    adInfo.creativeUrl = json.getString(JSONKey.creativeUrl);
            }

            //set Toolbar
            ToolbarHandler handler = new ToolbarHandler(adInfo, json);
            handler.set(JSONKey.enable_download, ShareType.download, JSONKey.downloadUrl);
            handler.set(JSONKey.enable_fb, ShareType.fb, JSONKey.fbUrl);
            handler.set(JSONKey.enable_line, ShareType.line, JSONKey.lineTxt);
            handler.set(JSONKey.enable_phone, ShareType.phone, JSONKey.phone);
            handler.set(JSONKey.enable_url, ShareType.url, JSONKey.phone);
            adInfo = handler.getAdInfo();

            adInfo.fbShortUrl = json.getString(JSONKey.fbPageId);

            //iBeacon
            JSONArray array = json.getJSONArray(JSONKey.iBeacons);
            for (int i = 0; i < array.length(); i++) {
                String uuid = array.getString(i);
                adInfo.iBeacons.add(uuid);
            }
            adInfo.iBeaconsUrl = json.getString(JSONKey.iBeaconUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return adInfo;
    }

    class ToolbarHandler {

        private CommonData adInfo;
        private MyJSONObject object;

        public ToolbarHandler(CommonData adInfo, MyJSONObject object) {
            this.adInfo = adInfo;
            this.object = object;
        }

        public void set(JSONKey enableKey, ShareType shareType, JSONKey valueKey) throws JSONException {
            if (isEnable(enableKey)) {
                int position = shareType.getIdx();
                adInfo.endingCard[position] = true;
                adInfo.endingCardText[position] = object.getString(valueKey);
            }
        }

        private boolean isEnable(JSONKey enableKey) throws JSONException {
            if (enableKey == JSONKey.enable_line && !ToolBarAction.checkLineInstalled(context)) {
                return false;
            } else if (enableKey == JSONKey.enable_phone
                    && SDKUtil.getSimState(context) != TelephonyManager.SIM_STATE_READY) {
                return false;
            }

            return object.getBoolean(enableKey);
        }

        public CommonData getAdInfo() {
            return adInfo;
        }

    }


}
