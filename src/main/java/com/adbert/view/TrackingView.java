package com.adbert.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adbert.util.SDKUtil;

import java.net.HttpCookie;
import java.util.List;

/**
 * Created by chihhan on 16/10/26.
 */
public class TrackingView extends WebView {

    public TrackingView(Context context) {
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.setWebViewClient(new WebViewClientImpl());
        this.setVisibility(View.GONE);
    }

    @Override
    public void loadUrl(String url) {
        setCookie(getContext(), url);
        this.getLayoutParams().width = 0;
        this.getLayoutParams().height = 0;
        this.setVisibility(View.VISIBLE);
        super.loadUrl(url);
    }

    private final class WebViewClientImpl extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (getContext() != null && view != null && view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }
    }

    private void setCookie(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        List<HttpCookie> sessionCookie = SDKUtil.msCookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : sessionCookie) {
            String cookieString = cookie.getName() + "=" + cookie.getValue();
            cookieManager.setCookie(url, cookieString);
            CookieSyncManager.getInstance().sync();
        }
    }

}
