package com.adbert.util.data;

import android.Manifest;
import android.content.Context;

import com.adbert.util.SDKUtil;
import com.adbert.util.enums.AdbertADType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonData implements Serializable {

    private static final long serialVersionUID = 4012816993561969157L;
    public AdbertADType type = null;  //displayType
    public AdbertADType realType = null;
    public int returnTime = 10000;
    public String pid = "";
    public String banner = "";
    public String mediaSource = "";
    public String mediaSourceSmall = "";
    public String cpmBannerImg = "";
    public boolean[] endingCard = new boolean[]{false, false, false, false, false};
    public String[] endingCardText = new String[]{"", "", "", "", ""};
    public boolean url_openInAPP = false;
    public boolean shouldOpen = false;
    public boolean biged = false;
    public boolean returned = false;
    public boolean volumeOpen = false;
    public boolean absolute = false;
    public String returnUrl = "";
    public String appId = "";
    public String appKey = "";
    public String shareReturnUrl = "";
    public String exposureUrl = "";
    public String creativeUrl = "";
    public String actionReturnUrl = "";
    public String durationReturnUrl = "";
    public boolean isFullScreen = false;
    public boolean special = false;
    public boolean adServing = false;
    public String uuId = "";
    public String gaUrl = "";
    public String fbShortUrl = "";
    public int goWhere = 1;
    public List<String> iBeacons = new ArrayList<>();
    public String iBeaconsUrl = "";

    public void setMediaType(AdbertADType realType, int defaultCreative) {
        this.realType = realType;
        if (defaultCreative > 0) {
            if (realType == AdbertADType.banner) {
                type = AdbertADType.banner_web;
            } else if (realType == AdbertADType.cpm_banner) {
                type = AdbertADType.cpm_web;
            }
        } else {
            type = realType;
        }
    }

    public boolean isRunScanner(Context context) {
        if ( SDKUtil.checkPermission(context, Manifest.permission.BLUETOOTH)
                && SDKUtil.checkPermission(context, Manifest.permission.BLUETOOTH_ADMIN) && iBeacons.size() > 0) {
            return true;
        }
        return false;
    }
}
