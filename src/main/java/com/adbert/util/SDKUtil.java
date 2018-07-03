package com.adbert.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.RelativeLayout;

import com.adbert.util.enums.LogMsg;
import com.adbert.view.LogoImage;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SDKUtil {

    //export to Jar Setting
    final public static boolean log = true; //should be false while publish
    final public static String version = "3.2.0n";   //should end with f or n
    final public static String build = "1";
//    final public static boolean returnClickEvent = false;

    final private static String tag = "Adbert";
    final private static String tag_inters = "Adbert_interstitial";
    final public static double ciScale = 0.04;
    final public static double closeRangeScale = 1.4, closeScale = 0.6;
    public static boolean firstRequestNonMediationAD = false;
    public static boolean isAllus() {
        return version.endsWith("f");
    }

    public static boolean isGIF(String path) {
        if (!path.isEmpty() && (path.endsWith(".gif") || path.contains(".gif?"))) {
            return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    public static boolean checkPermission(Context context, String permission) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int hasWriteContactsPermission = context.checkSelfPermission(permission);
                if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
                    return false;
                else
                    return true;
            } else {
                PackageManager pm = context.getPackageManager();
                int hasPerm = pm.checkPermission(permission, context.getPackageName());
                if (hasPerm != PackageManager.PERMISSION_GRANTED)
                    return false;
                else
                    return true;
            }
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        return false;
    }

    public static boolean isSDKunder14() {
        if (Build.VERSION.SDK_INT < 14) {
            return true;
        }
        return false;
    }

    public static boolean checkResourceString(String... urls) {
        for (int i = 0; i < urls.length; i++) {
            if (urls[i] == null || urls[i].isEmpty() || urls[i].endsWith("/")) {
                return false;
            }
        }
        return true;
    }

    public static void setLogo(Context context, int logow, ViewGroup parent, boolean isRLayout) {
        LogoImage logo = new LogoImage(context, logow);
        parent.addView(logo);
        if (isRLayout) {
            ((RelativeLayout.LayoutParams) logo.getLayoutParams())
                    .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }

    private static void saveCookie(int responseCode, HttpURLConnection connection) {
        if (responseCode == 200) {
            // java.net.CookieHandler.setDefault(msCookieManager);
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
        }

        Map<String, List<String>> headerFields = connection.getHeaderFields();

        Set<String> headerFieldsSet = headerFields.keySet();
        Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();

        while (hearerFieldsIter.hasNext()) {

            String headerFieldKey = hearerFieldsIter.next();

            if ("Set-Cookie".equalsIgnoreCase(headerFieldKey)) {

                List<String> headerFieldValue = headerFields.get(headerFieldKey);

                for (String headerValue : headerFieldValue) {

                    // System.out.println("Cookie Found...");

                    String[] fields = headerValue.split(";\\s*");

                    String cookieValue = fields[0];
                    String expires = null;
                    String path = null;
                    String domain = null;
                    boolean secure = false;

                    // Parse each field
                    for (int j = 1; j < fields.length; j++) {
                        if ("secure".equalsIgnoreCase(fields[j])) {
                            secure = true;
                        } else if (fields[j].indexOf('=') > 0) {
                            String[] f = fields[j].split("=");
                            if ("expires".equalsIgnoreCase(f[0])) {
                                expires = f[1];
                            } else if ("domain".equalsIgnoreCase(f[0])) {
                                domain = f[1];
                            } else if ("path".equalsIgnoreCase(f[0])) {
                                path = f[1];
                            }
                        }

                    }

                    // System.out.println("cookieValue:" + cookieValue);
                    // System.out.println("expires:" + expires);
                    // System.out.println("path:" + path);
                    // System.out.println("domain:" + domain);
                    // System.out.println("secure:" + secure);

                    // System.out.println("*****************************************");
                }

            }

        }
    }

    private static void setToWebCookie(String url) {
        // CookieManager.getInstance().removeAllCookie();
        // CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        List<HttpCookie> sessionCookie = msCookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : sessionCookie) {
            String cookieString = cookie.getName() + "=" + cookie.getValue();
            cookieManager.setCookie(url, cookieString);
            try {
                CookieSyncManager.getInstance().sync();
            } catch (Exception e) {
                if (log) {
                    SDKUtil.logException(e);
                }
            }
        }
    }

    public static java.net.CookieManager msCookieManager = new java.net.CookieManager();


    public static void initCookie(Context context) {
        CookieSyncManager.createInstance(context);
        // unrelated, just make sure cookies are generally allowed
        CookieManager.getInstance().setAcceptCookie(true);

        // magic starts here
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null,
                java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
    }

    public interface GetUUIDListener {
        void onResult(String result);
    }

    public static boolean isPortrait(Context context) {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public static int getBtnWidth(Context context, boolean screenPortrait, int btnh) {
        ScreenSize screenSize = new ScreenSize(context);
        float pWidth = screenSize.getScreenWidth();
        float pHeight = screenSize.getScreenHeight();
        float w = screenPortrait ? pWidth : pHeight;
        return (int) ((w / (float) 480) * btnh);
    }

    static String UUID = "";

    public static void getUUID(final Context context, final GetUUIDListener listener) {
        if (!UUID.isEmpty()) {
            listener.onResult(UUID);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String uuid = "";
                    try {
                        AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                        // boolean isLAT = adInfo.isLimitAdTrackingEnabled();
                        uuid = adInfo.getId();
                        if (uuid == null) {
                            uuid = "";
                        }
                    } catch (ClassCastException e) {
                        SDKUtil.logException(e);
                        uuid = "";
                    } catch (Exception e) {
                        SDKUtil.logException(e);
                        uuid = "";
                    }
                    if (uuid.isEmpty()) {
                        SDKUtil.logWarning(LogMsg.UUID_EMPTY.getValue());
                    }
                    UUID = uuid;
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(UUID);
                        }
                    });
                }
            }).start();
        }
    }

    public static boolean connectable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void logException(Exception e) {
        if (log)
            e.printStackTrace();
    }

    public static void logTestMsg(String msg) {
        if (log)
            Log.e(tag, msg);
    }

    public static void logWarning(String msg) {
        Log.w(tag, msg);
    }

    public static void logInfo(String msg) {
        Log.i(tag, msg);
    }

    public static void logTestMsg_inters(String msg) {
        if (log)
            Log.e(tag_inters, msg);
    }

    public static void logWarning_inters(String msg) {
        Log.w(tag_inters, msg);
    }

    public static void logInfo_inters(String msg) {
        Log.i(tag_inters, msg);
    }

    public static int countUIH(float targetW, int oriW, int oriH) {
        return (int) ((targetW / oriW) * oriH);
    }

    private static String getFileNameFromUrl_old(Context context, String url) {
        String folder = context.getCacheDir().getAbsolutePath();
        String result = url;
        if (!url.isEmpty() && url.contains("/")) {
            File file = new File(context.getCacheDir() + "/ADBERT/");
            if (!file.exists()) {
                file.mkdirs();
            }
            String tmp = new String(url);
            String fileFolder = tmp.substring(tmp.substring(0, tmp.lastIndexOf("/")).lastIndexOf("/"),
                    tmp.lastIndexOf("/"));
            String fileNmae = tmp.substring(tmp.lastIndexOf("/") + 1);
            result = folder + "/ADBERT" + fileFolder + "_" + fileNmae;
            return result;
        }
        return result;
    }

    public static void saveRedirectURL(String url, String finalUrl) {
        if (!url.equals(finalUrl)) {
            redirectUrl.put(url, finalUrl);
        }
    }

    private static TelephonyManager mTelephonyManager;

    public static int getSimState(Context context) {
        if (null == mTelephonyManager)
            mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getSimState();
    }

    private static byte[] getHash(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public static String bin2hex(String strForEncrypt) {
        byte[] data = getHash(strForEncrypt);
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    static Map<String, String> redirectUrl = new HashMap<>();

    public static String getFileNameFromUrl(Context context, String url) {
        if (redirectUrl.get(url) != null) {
            url = redirectUrl.get(url);
        }
        File folder = new File(context.getCacheDir(), "/ADBERT/");
        //
        if (url.endsWith("mp4"))
            folder = new File(folder, "/video/");
        else
            folder = new File(folder, "/others/");
        if (!folder.exists())
            folder.mkdirs();
        //
        String urlCopy = new String(url);
        String fileFolder = urlCopy.substring(
                urlCopy.substring(0, urlCopy.lastIndexOf("/")).lastIndexOf("/"), urlCopy.lastIndexOf("/"));
        String fileName = fileFolder + "_" + urlCopy.substring(urlCopy.lastIndexOf("/") + 1);
        //
        File newFile = new File(folder, fileName);
        //
        File oldFile = new File(getFileNameFromUrl_old(context, url));
        if (oldFile.exists()) {
            try {
                oldFile.renameTo(newFile);
            } catch (Exception e) {
            }
        }
        return newFile.getAbsolutePath();
    }

    public static boolean savePic(Bitmap b, String url, String finalUrl, String savePath) {
        saveRedirectURL(url, finalUrl);
        return savePic(b, savePath);
    }

    public static boolean savePic(Bitmap b, String savePath) {
        if (new File(savePath).exists()) {
            return true;
        }
        boolean isJPG = true;
        if (isGIF(savePath)) {
            isJPG = false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savePath);
            if (null != fos) {
                if (isJPG) {
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } else {
                    b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            logException(e);
            return false;
        }
        return (new File(savePath).exists());
    }

    final static String timeFormat = "yyyyMMddHHmmssSSS";

    public static String getTime() {
        SimpleDateFormat df = new SimpleDateFormat(timeFormat, Locale.getDefault());
        Calendar c = Calendar.getInstance();
        return df.format(c.getTime());
    }

    public static String getLastWeekTime() {
        SimpleDateFormat df = new SimpleDateFormat(timeFormat, Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);
        return df.format(c.getTime());
    }

    public static Date getDate(String str) {
        SimpleDateFormat df = new SimpleDateFormat(timeFormat, Locale.getDefault());
        Date date = null;
        try {
            date = df.parse(str);
        } catch (ParseException e) {
            SDKUtil.logException(e);
        }
        return date;
    }

    public static int getStatusHeight(Activity activity) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject)
                        .toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
            }
        }
        return statusHeight;
    }
}
