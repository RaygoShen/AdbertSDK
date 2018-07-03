package com.adbert.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adbert.util.SDKUtil;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.list.CustomViewListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GIFView extends RelativeLayout {

    WebView webView;

    public GIFView(Context context, final CustomViewListener listener, AdbertADType type) {
        super(context);
        webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {
        });
        webView.clearCache(true);
        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setUseWideViewPort(true);
        websettings.setLoadWithOverviewMode(true);
        websettings.setDefaultTextEncodingName("utf-8");
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new WebViewClient());
        webView.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVisibility(View.GONE);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                webView.setVisibility(View.VISIBLE);
                listener.onPageFinished();
            }
        });
        this.addView(webView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        cover = new RelativeLayout(context);
        this.addView(cover, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (type != AdbertADType.cpm_video) {
            cover.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 5; i++) {
                        if (videoInfo.endingCard[i]) {
                            listener.endingCardAction(i);
                            break;
                        }
                    }
                }
            });
        }
    }

    public void destroy() {
        if (webView != null) {
            webView.destroy();
        }
    }

    RelativeLayout cover;

    public RelativeLayout getCover() {
        return cover;
    }

    CommonData videoInfo;

    public void load(String url, CommonData videoInfo) {
        this.videoInfo = videoInfo;
        String yourData = "<!doctype html><html><head><meta charset=\"UTF-8\">"
                + "<title>Untitled Document</title><style>body{	margin:0;	padding:0;	}</style></head><body>"
                + "<div><img src=\"" + url + "\" width=\"100%\"//></div></body></html>";
        try {
            webView.loadData(URLEncoder.encode(yourData, "utf8").replaceAll("\\+", " "),
                    "text/html", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SDKUtil.logException(e);
        }
    }
}
