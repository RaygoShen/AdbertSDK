package com.adbert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.adbert.util.BeaconScanner;
import com.adbert.util.DataParser;
import com.adbert.util.OpenFileUtil;
import com.adbert.util.ReturnDataUtil;
import com.adbert.util.SDKUtil;
import com.adbert.util.ToolBarAction;
import com.adbert.util.data.CommonData;
import com.adbert.util.enums.AdbertADType;
import com.adbert.util.enums.ShareType;
import com.adbert.util.list.CustomViewListener;
import com.adbert.view.AdbertWebView;
import com.adbert.view.AdbertWebView.OpenFileListener;
import com.adbert.view.CPMBannerView;
import com.adbert.view.ExpandVideoView;
import com.adbert.view.TrackingView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class AdbertInterstitialActivity extends Activity {

    private float pWidth, pHeight, pDensity;
    private boolean screenPortrait, inVideo, inCPMBanner;
    private int btnh = 50;
    private WebView webView;
    private CommonData ad;
    private ExpandVideoView cpmVideo;
    private CPMBannerView cpmBanner;
    private AdbertWebView webViewll;
    private AdbertWebView cpmWebView;
    private boolean isCPMWeb = false;
    private String cameraFunctionName = "";
    private ValueCallback<Uri> mUploadMessage;
    private final int FILECHOOSER_RESULTCODE = 1;
    private final int FILECHOOSER_RESULTCODE2 = 2;
    public final int INPUT_FILE_REQUEST_CODE = 10;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private BeaconScanner scanner;
//    private String uuId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        init();

        //start  scanner
        SDKUtil.logTestMsg("BeaconScanner Activity Start");
        if (ad != null && ad.isRunScanner(this)) {
            scanner = new BeaconScanner(ad.iBeacons);
            scanner.startScan();
        }
    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        pWidth = dm.widthPixels;
        pHeight = dm.heightPixels;
        pDensity = dm.density;

        screenPortrait = SDKUtil.isPortrait(this);
        btnh = SDKUtil.getBtnWidth(this, screenPortrait, btnh);

        if (getIntent().getExtras() != null && getIntent().hasExtra("jsonStr")) {
            String jsonStr = getIntent().getExtras().getString("jsonStr");
            try {
                ad = new CommonData();
                SDKUtil.getUUID(this, new SDKUtil.GetUUIDListener() {
                    @Override
                    public void onResult(String result) {
                        ad.uuId = result;
                    }
                });
                ad = new DataParser(getApplicationContext()).parse(jsonStr, screenPortrait);
                if (ad.type == AdbertADType.cpm_banner) {
                    showCPMBanner();
                } else if (ad.type == AdbertADType.cpm_video) {
                    showVideo();
                } else if (ad.type == AdbertADType.cpm_web) {
                    showCPMWebView();
                }
                if (!ad.returned) {
                    ad.returned = true;
                    ReturnDataUtil.exposureEvent(this, ad, new Runnable() {
                        @Override
                        public void run() {
                            ad.returned = false;
                        }
                    });
                }
            } catch (JSONException e) {
                SDKUtil.logException(e);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
        } else {
            SDKUtil.logTestMsg("!jsonStr");
        }
    }

    private void showCPMBanner() {
        inCPMBanner = true;
        cpmBanner = new CPMBannerView(this, screenPortrait, ad, pWidth, pHeight, btnh,
                cpmListener);
        setContentView(cpmBanner);
        if (!ad.gaUrl.isEmpty()) {
            setTrackingView(cpmBanner);
        }
    }

    private void showVideo() {
        inVideo = true;
        boolean hideCI = false;
        if (getIntent().hasExtra("hideCI") && getIntent().getExtras().getBoolean("hideCI"))
            hideCI = true;
        cpmVideo = new ExpandVideoView(this, ad, btnh, cpmListener);
        cpmVideo.showCPMVideo(hideCI);
//        cpmVideo = new ExpandVideoView(this, screenPortrait, videoInfo, btnh, pWidth, pHeight, cpmListener,
//                hideCI);
        setContentView(cpmVideo);
        if (!ad.gaUrl.isEmpty()) {
            setTrackingView(cpmVideo);
        }
    }

    private void showCPMWebView() {
        isCPMWeb = true;
        cpmWebView = new AdbertWebView(this, ad, cpmListener).setOpenFileListener(
                openFileListener).load(ad.creativeUrl, ad.isFullScreen, btnh, true);
        cpmWebView.getWebView().setWebChromeClient(new MyWebClient());
        setContentView(cpmWebView);
    }

    private void showWeb(String url) {
        pause();
        webViewll = new AdbertWebView(this, ad, cpmListener).setOpenFileListener(
                openFileListener).load(url, true, btnh);
        if (ad.type == AdbertADType.cpm_banner) {
            cpmBanner.addView(webViewll);
        } else {
            cpmVideo.addView(webViewll);
        }
        webView = webViewll.getWebView();
    }

    private TrackingView trackingView;

    private void setTrackingView(ViewGroup viewGroup) {
        if (getApplicationContext() != null) {
            trackingView = new TrackingView(this);
            viewGroup.addView(trackingView);
            trackingView.loadUrl(ad.gaUrl);
        }
    }

    OpenFileListener openFileListener = new OpenFileListener() {

        @Override
        public void onCamera(boolean camera, String functionName) {
            if (!functionName.isEmpty()) {
                AdbertInterstitialActivity.this.cameraFunctionName = functionName;
                openFile(camera, FILECHOOSER_RESULTCODE2);
            }
        }
    };

    CustomViewListener cpmListener = new CustomViewListener() {

        public void setLogo(ViewGroup parent, boolean isRLayout) {
            if (!ad.special) {
                float pw = screenPortrait ? pWidth : pHeight;
                SDKUtil.setLogo(getApplicationContext(), (int) (pw * SDKUtil.ciScale), parent, isRLayout);
            }
        }

        public void finish() {
            AdbertInterstitialActivity.this.finish();
        }

        public void endingCardAction(int position) {
            AdbertInterstitialActivity.this.endingCardAction(position);
        }

        public void closeWeb() {
            if (isCPMWeb) {
                AdbertInterstitialActivity.this.finish();
            } else {
                ((ViewGroup) webViewll.getParent()).removeView(webViewll);
                webViewll = null;
                resume();
            }
        }

        public void closeVideo() {
            AdbertInterstitialActivity.this.finish();
        }
    };

    private void endingCardAction(int type) {
        ReturnDataUtil.shareReturn(this, ad, ShareType.init.fromInt(type));
        broadcast("click");
        ToolBarAction.getToolBar(this).toolbarAction(ad, type, new ToolBarAction.OpenInAppListener() {
            @Override
            public void open(String url) {
                showWeb(url);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else if (webViewll != null && webViewll.getVisibility() == View.VISIBLE) {
                if (ad.type == AdbertADType.cpm_banner) {
                    cpmBanner.removeView(webViewll);
                } else {
                    cpmVideo.removeView(webViewll);
                }
                webViewll = null;
                resume();
            } else if (isCPMWeb && cpmWebView != null && cpmWebView.getWebView().canGoBack()) {
                cpmWebView.getWebView().goBack();
            } else {
                finish();
            }
            return false;
        } else
            return super.dispatchKeyEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == INPUT_FILE_REQUEST_CODE) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == this.mUploadMessage) {
                return;
            }
            Uri result = null;
            try {
                if (resultCode != RESULT_OK) {
                    result = null;
                } else {
                    if (intent != null) {
                        result = Uri.parse(OpenFileUtil.getRealPathFromURI(this, intent.getData()));
                    } else
                        result = mCapturedImageURI;
                }
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE2) {
            Uri result = null;
            try {
                if (resultCode != RESULT_OK) {
                    result = null;
                } else {
                    if (intent != null) {
                        result = intent.getData();
                    } else
                        result = mCapturedImageURI;
                    uploadFile(result, cameraFunctionName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void uploadFile(Uri uri, String functionName) {
        String uriPath = OpenFileUtil.getRealPathFromURI(this, uri);
        if (uriPath.isEmpty())
            uriPath = uri.getPath();
        String encodedImage = OpenFileUtil.encodeTobase64(this, uri, uriPath);
        String src = "data:image/jpeg;base64," + encodedImage;
        cpmWebView.getWebView().loadUrl("javascript:" + functionName + "('" + src + "');");
    }

    private void openFile(boolean camera, int requestCode) {
        if (camera
                && SDKUtil.checkPermission(AdbertInterstitialActivity.this, android.Manifest.permission.CAMERA)
                && SDKUtil.checkPermission(AdbertInterstitialActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File cameraDataDir = new File(getExternalCacheDir(), "browser-cache");
            cameraDataDir.mkdirs();
            String mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
                    + System.currentTimeMillis() + ".jpg";
            mCapturedImageURI = Uri.fromFile(new File(mCameraFilePath));
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(cameraIntent, requestCode);
        } else if (SDKUtil.checkPermission(AdbertInterstitialActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(i, requestCode);
        }
    }

    public class MyWebClient extends WebChromeClient {

        public void openFileChooser(ValueCallback<Uri> uploadMsg, boolean camera) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            mUploadMessage = uploadMsg;
            openFile(camera, FILECHOOSER_RESULTCODE);
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, false);
        }

        // For Android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            if (capture.equals("camera") || (acceptType.isEmpty() && capture.equals("*"))
                    || acceptType.contains("capture=camera")) {
                openFileChooser(uploadMsg, true);
            } else {
                openFileChooser(uploadMsg, false);
            }
        }

        @SuppressLint("NewApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            if (fileChooserParams.isCaptureEnabled()
                    && SDKUtil.checkPermission(AdbertInterstitialActivity.this,
                    android.Manifest.permission.CAMERA)
                    && SDKUtil.checkPermission(AdbertInterstitialActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = OpenFileUtil.createImageFile(getApplicationContext());
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        SDKUtil.logException(ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                    startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
                }
            } else if (SDKUtil.checkPermission(AdbertInterstitialActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                intentArray = new Intent[0];
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final android.webkit.JsResult result) {
            new AlertDialog.Builder(AdbertInterstitialActivity.this).setTitle("").setMessage(message)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {

                        public void onClick(DialogInterface dialog, int wicht) {
                            result.confirm();
                        }
                    }).setCancelable(false).create().show();
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
        broadcast("close");
    }

    private void broadcast(String action) {
        String pid = ad != null ? ad.pid : "";
        Intent intent = new Intent("ad" + pid);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        intent.putExtra("action", action);
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
        if (scanner != null) {
            scanner.stopScan();
        }
    }

    private void pause() {
        if (inVideo && cpmVideo != null) {
            cpmVideo.pause();
        } else if (cpmWebView != null) {
            cpmWebView.pause();
        } else if (webViewll != null) {
            webViewll.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
        SDKUtil.logTestMsg("onResume()");
        if (scanner != null) {
            scanner.startScan();
        }
    }

    private void resume() {
        if (inVideo && cpmVideo != null) {
            cpmVideo.resume();
        } else if (cpmWebView != null) {
            cpmWebView.resume();
        } else if (webViewll != null) {
            webViewll.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //return scan result
        if (scanner != null && ad != null) {
            String result = scanner.getResult(ad.uuId);
            ReturnDataUtil.iBeaconReturn(this, result, ad);
            scanner.stopScan();
            scanner = null;
        }
        if (inVideo && cpmVideo != null) {
            ReturnDataUtil.durationReturn(getApplicationContext(), ad, cpmVideo.getSeekTo());
            cpmVideo.destroy();
        }
        if (inCPMBanner && cpmBanner != null) {
            cpmBanner.destroy();
        }
        if (webView != null) {
            webView.destroy();
        }
        if (cpmWebView != null) {
            cpmWebView.destroy();
            cpmWebView.getWebView().destroy();
        }
        if (webViewll != null) {
            webViewll.destroy();
        }
        if (trackingView != null) {
            trackingView.destroy();
        }

    }
}
