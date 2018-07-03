package com.adbert.util.list;

import com.adbert.AdbertListener;
import com.adbert.AdbertOrientation;
import com.adbert.ExpandVideoPosition;
import com.google.android.gms.ads.AdSize;

public interface BaseBannerMethod {

    void setTestMode();

    void setExpandVideo(ExpandVideoPosition expandVideoPosition);

    void setMode(AdbertOrientation orientation);

    void setFullScreen(boolean fullscreen);

    void setBannerSize(int bannerWidth);

    void setBannerSize(AdSize adSize);

    void setListener(AdbertListener listener);

    void start();

    void destroy();

    void pause();

    void resume();

    String getVersion();

    void setPageInfo(String pageInfo);

    void hideCI();
}
