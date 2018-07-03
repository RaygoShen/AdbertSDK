package com.adbert.util.enums;

/**
 * Created by chihhan on 2017/9/22.
 */

public enum JSONKey {
    returnUrl, appId, appKey, shareReturnUrl, exposureUrl, IniExpand, Y, pid, mediaType, bannerUrl, absolute,
    enable_line, enable_url, linkUrl,  enable_fb, fbUrl, enable_phone, phone, enable_download,fbPageId,
    downloadUrl, url_open,lineTxt, creativeUrl, fillbannerUrl, ad, iBeacons, iBeaconUrl, gaUrl, adServing,
    defaultValid, actionReturnUrl, durationReturnUrl, enable_beaconScan, defaultCreative, fillbannerUrl_P,
    fillbannerUrl_L, mediaSrc, mediaSrc_S, location, id, range, redirectUrl, LBS_AD, LBS_AD_LIMIT, isFullScreen;

    private String code = "";

    JSONKey() {
        this.code = this.toString();
    }

    public String v() {
        return code;
    }
}
