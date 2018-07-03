package com.adbert.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Vibrator;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adbert.util.ReturnDataUtil;
import com.adbert.util.SDKUtil;
import com.adbert.util.ScreenSize;
import com.adbert.util.SensorMode;
import com.adbert.util.SensorMode.ConclusionListener;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.enums.Colors;
import com.adbert.util.enums.SensorType;
import com.adbert.util.enums.ShareType;
import com.adbert.util.list.CustomViewListener;

import java.net.HttpCookie;
import java.util.List;

@SuppressLint("SetJavaScriptEnabled")
public class AdbertWebView extends RelativeLayout {

    public interface OpenFileListener {
        public void onCamera(boolean camera, String functionName);
    }

    Context context;
    CustomViewListener listener;
    ScreenSize screenSize;
    OpenFileListener openFileListener;
    CommonData videoInfo;
    HelpWebView webView;

    public AdbertWebView(Context context, CommonData videoInfo, CustomViewListener listener) {
        super(context);
        this.context = context;
        this.videoInfo = videoInfo;
        this.listener = listener;
        screenSize = new ScreenSize(context);
    }

    public AdbertWebView setOpenFileListener(OpenFileListener openFileListener) {
        this.openFileListener = openFileListener;
        return this;
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

    public AdbertWebView load(String url, boolean fillparent, int btnh, boolean... isH5) {
        setCookie(context, url);
        webView = new HelpWebView(context);
        this.addView(webView);
        if (fillparent) {
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        } else {
            setBackgroundColor(Colors.cpmBg.parseColor());
            webView.getLayoutParams().width = screenSize.getPercentOfWidth(0.9);
            webView.getLayoutParams().height = screenSize.getHeight(screenSize.getPercentOfWidth(0.9), 2, 3);
            ((LayoutParams) webView.getLayoutParams())
                    .addRule(RelativeLayout.CENTER_IN_PARENT);
            listener.setLogo(this, true);
        }
        webView.setDownloadListener(new DownloadListener() {

            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
        WebSettings websettings = webView.getSettings();
        if (isH5.length == 0 && fillparent) {
            websettings.setSupportZoom(true);
            websettings.setBuiltInZoomControls(true);
        }
        websettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "Android");
        websettings.setDefaultTextEncodingName("utf-8");
        websettings.setGeolocationEnabled(true);
        websettings.setDomStorageEnabled(true);
        websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // websettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClientImpl());
        webView.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            websettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webView.loadUrl(url);
        webView.setInitialScale(1);
        if (btnh > 0) {
            setDelete(btnh);
        } else {
            // CPC
            if (false) {
                RelativeLayout cover = new RelativeLayout(context);
                this.addView(cover);
                cover.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                cover.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (videoInfo != null) {
                            for (int i = 0; i < 5; i++) {
                                if (videoInfo.endingCard[i]) {
                                    listener.endingCardAction(i);
                                    break;
                                } else
                                    SDKUtil.logTestMsg(ShareType.init.fromInt(i) + " false");
                            }
                        }
                    }
                });
            }
        }

        return this;
    }

    @SuppressWarnings("deprecation")
    public void load(String url) {
        if (Build.VERSION.SDK_INT < 18) {
            webView.clearView();
        } else {
            webView.loadUrl("about:blank");
        }
        webView.loadUrl(url);
    }

    private void setDelete(int btnh) {
        CustomCloseButton delete = new CustomCloseButton(getContext(), btnh);
        this.addView(delete);
        int size = (int) (btnh * SDKUtil.closeRangeScale);
        delete.getLayoutParams().width = size;
        delete.getLayoutParams().height = size;
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.closeWeb();
            }
        });

    }

    public class JavaScriptInterface {

        @JavascriptInterface
        public void shake(String functionName) {
            openSensor(SensorType.SHAKE, functionName);
        }

        @JavascriptInterface
        public void closeSensor() {
            if (sensorMode != null) {
                sensorMode.pause();
                sensorMode = null;
            }
        }

        @JavascriptInterface
        public void vibrate(int millisecond) {
            if (SDKUtil.checkPermission(context, android.Manifest.permission.VIBRATE)) {
                Vibrator vVi = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                vVi.vibrate(millisecond);
            }
        }

        @JavascriptInterface
        public void distance(String functionName) {
            openSensor(SensorType.DISTANCE, functionName);
        }

        @JavascriptInterface
        public void light(String functionName) {
            openSensor(SensorType.LIGHT, functionName);
        }

        @JavascriptInterface
        public void validReturn() {
            if (videoInfo != null) {
                if (videoInfo.realType == AdbertADType.banner) {
                    if (!videoInfo.returned) {
                        videoInfo.returned = true;
                        ReturnDataUtil.returnEvent(context, videoInfo, new Runnable() {
                            @Override
                            public void run() {
                                videoInfo.returned = false;
                            }
                        });
                    }
                } else {
                    ReturnDataUtil.returnEvent(context, videoInfo, null);
                }
            }
        }

        @JavascriptInterface
        public void shareReturn() {
            if (videoInfo != null) {
                ReturnDataUtil.shareReturn(context, videoInfo, ShareType.url.toString());
            }
        }

        @JavascriptInterface
        public void actionReturn() {
            if (videoInfo != null) {
                ReturnDataUtil.actionReturn(context, videoInfo, ShareType.url.toString());
            }
        }

        @JavascriptInterface
        public void openCamera(String functionName) {
            if (openFileListener != null)
                openFileListener.onCamera(true, functionName);
        }

        @JavascriptInterface
        public void openAlbum(String functionName) {
            if (openFileListener != null)
                openFileListener.onCamera(false, functionName);
        }

        @JavascriptInterface
        public void openBrowser(String url) {
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            context.startActivity(intent);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.chrome");
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                intent.setPackage(null);
                context.startActivity(intent);
            }
        }
    }

    public WebView getWebView() {
        return webView;
    }

    private final class WebViewClientImpl extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed(); // Ignore SSL certificate errors
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (listener != null)
                listener.onPageFinished();
            CookieSyncManager.getInstance().sync();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.compareTo("about:blank") == 0) {
                return true;
            } else if (url.endsWith(".mp4")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                tel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(tel);
                return true;
            } else if (url.startsWith("mailto:")) {
                String body = "";
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("text/plain");
                mail.putExtra(Intent.EXTRA_EMAIL, url);
                mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                mail.putExtra(Intent.EXTRA_TEXT, body);
                mail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mail);
                return true;
            }
            return false;
        }
    }

    SensorMode sensorMode;

    private void openSensor(SensorType sensorType, final String functionName) {
        sensorMode = new SensorMode(context, sensorType, new ConclusionListener() {

            @Override
            public void onFail(String msg) {
            }

            @Override
            public void onConclusion() {
                try {
                    webView.loadUrl("javascript:" + functionName);
                } catch (Exception e) {
                    SDKUtil.logException(e);
                }
            }

            @Override
            public void onConclusion(int value) {
                try {
                    webView.loadUrl("javascript:" + functionName + "('" + value + "');");
                } catch (Exception e) {
                    SDKUtil.logException(e);
                }
            }
        });
    }

    public void pause() {
        if (sensorMode != null)
            sensorMode.pause();
    }

    public void resume() {
        if (sensorMode != null)
            sensorMode.resume();
    }

    public void destroy() {
        if (sensorMode != null)
            sensorMode.pause();
    }
}
