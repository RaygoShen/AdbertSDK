package com.adbert.util.data;

import com.adbert.util.enums.ShareType;

import org.json.JSONObject;

/**
 * Created by chihhan on 2017/7/6.
 */

public class CommonNativeAD {
    public String returnUrl = "";
    public String exposureUrl = "";
    public String shareRetrunUrl = "";
    public String durationReturnUrl = "";
    public String gaUrl = "";
    public String appId = "";
    public String appKey = "";
    public String uuId = "";
    public String pid = "";
    public boolean returned = false;
    public boolean openBrowser = false;
    public ShareType shareType = ShareType.init; //native_normal
    public String shareContent = ""; //native_normal
    public JSONObject publisherData = new JSONObject(); //native_normal
    public String mediaSrc = ""; //native_video
    public boolean absolute = false; //native_video
    public int returnTime = 10000; //native_video
    public boolean[] endingCard = new boolean[]{false, false, false, false, false};
    public String[] endingCardText = new String[]{"", "", "", "", ""};
    public String fbShortUrl = "";

    public CommonData getCommonAD() {
        CommonData ad = new CommonData();
        ad.volumeOpen = false;
        ad.absolute = this.absolute;
        ad.returnUrl = this.returnUrl;
        ad.shareReturnUrl = this.shareRetrunUrl;
        ad.appId = this.appId;
        ad.appKey = this.appKey;
        ad.uuId = this.uuId;
        ad.pid = this.pid;
        ad.fbShortUrl = this.fbShortUrl;
        ad.returned = this.returned;
        ad.url_openInAPP = !this.openBrowser;
        ad.durationReturnUrl = this.durationReturnUrl;
        ad.returnTime = this.returnTime;
        ad.mediaSource = this.mediaSrc;
        ad.url_openInAPP = !this.openBrowser;
        ad.endingCard = this.endingCard;
        ad.endingCardText = this.endingCardText;
        ad.exposureUrl = this.exposureUrl;
        return ad;
    }
}
