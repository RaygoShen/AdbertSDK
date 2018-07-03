package com.adbert.util.enums;

import java.io.Serializable;

public enum AdbertADType implements Serializable {
    video, banner, cpm_video, cpm_banner, cpm_web, banner_web;
    //cpm_web and banner_web is android private value

    public AdbertADType getTypeFromStr(String mediaType) {
        if (mediaType.equals(video.toString())) {
            return video;
        } else if (mediaType.equals(banner.toString())) {
            return banner;
        } else if (mediaType.equals(cpm_video.toString())) {
            return cpm_video;
        } else if (mediaType.equals(cpm_banner.toString())) {
            return cpm_banner;
        }
        return null;
    }
}
