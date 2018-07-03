package com.adbert.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.adbert.util.data.CommonData;
import com.adbert.util.enums.ShareType;

import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

/**
 * Created by chihhan on 2017/7/10.
 */

public class ToolBarAction {

    public interface OpenInAppListener {
        void open(String url);
    }

    private Context context;

    private ToolBarAction(Context context) {
        this.context = context;
    }

    public static ToolBarAction getToolBar(Context context) {
        return new ToolBarAction(context);
    }

    public void toolbarAction(CommonData ad, int type, OpenInAppListener listener) {
        //return valid & share
        ShareType shareType = ShareType.init.getTypeFromPosition(type);
        //action
        String shareContent = ad.endingCardText[type];
        if (shareType == ShareType.download) { //0
            openDownloadURL(context, shareContent);
        } else if (shareType == ShareType.url) {//1
            if (ad.url_openInAPP && listener != null) {
                listener.open(shareContent);
            } else {
                openBrowser(context, shareContent);
            }
        } else if (shareType == ShareType.phone) {//2
            call(context, shareContent);
        } else if (shareType == ShareType.fb) {//3
            String url = getFacebookPageURL(context, shareContent, ad.fbShortUrl);
            if (url.startsWith("fb://")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                openBrowser(context, url);
            }
        } else if (shareType == ShareType.line) {//4
            shareToLine(context, shareContent);
        }
    }

    private String getPackageName(String url) {
        Uri uri = Uri.parse(url);
        String server = uri.getAuthority();
        String path = uri.getPath();
        String protocol = uri.getScheme();
        Set<String> args = uri.getQueryParameterNames();
        return uri.getQueryParameter("id");
    }

    private void openDownloadURL(Context context, String url) {
        String storeKeyPoint = "play.google.com/store/apps/details?id=";
        if (url.startsWith("http://" + storeKeyPoint) || url.startsWith("https://" + storeKeyPoint)) {
            String packageName = getPackageName(url);
//            Log.d("TEST", "packageName = " + packageName);
            Intent intent;
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                context.startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                openBrowser(context, url);
            }
        } else {
            openBrowser(context, url);
        }
    }

    private void openBrowser(Context context, String url) {
        try {
            Intent i = new Intent();
            i.setComponent(getDefaultBrowserComponent(context));
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (Exception e) {
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

    private ComponentName getDefaultBrowserComponent(Context context) {
        Intent i = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .setData(new Uri.Builder()
                        .scheme("http")
                        .authority("x.y.z")
                        .appendQueryParameter("q", "x")
                        .build()
                );
        PackageManager pm = context.getPackageManager();
        ResolveInfo default_ri = pm.resolveActivity(i, 0); // may be a chooser
        ResolveInfo browser_ri = null;
        List<ResolveInfo> rList = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : rList) {
            if (ri.activityInfo.packageName.equals(default_ri.activityInfo.packageName)
                    && ri.activityInfo.name.equals(default_ri.activityInfo.name)
                    ) {
                return ri2cn(default_ri);
            } else if ("com.android.browser".equals(ri.activityInfo.packageName)) {
                browser_ri = ri;
            }
        }
        if (browser_ri != null) {
            return ri2cn(browser_ri);
        } else if (rList.size() > 0) {
            return ri2cn(rList.get(0));
        } else if (default_ri == null) {
            return null;
        } else {
            return ri2cn(default_ri);
        }
    }

    private static ComponentName ri2cn(ResolveInfo ri) {
        return new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
    }

    private void shareToLine(Context context, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(intent);
    }

    private static final String PACKAGE_NAME = "jp.naver.line.android";
    private static final String CLASS_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity";

    public static boolean checkLineInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> m_appList = pm.getInstalledApplications(0);
        boolean lineInstallFlag = false;
        for (ApplicationInfo ai : m_appList) {
            if (ai.packageName.equals(PACKAGE_NAME)) {
                lineInstallFlag = true;
                break;
            }
        }
        return lineInstallFlag;
    }

    private void call(Context context, String shareContent) {
        try {
            Intent ie = new Intent("android.intent.action.CALL", Uri.parse("tel:" + shareContent));
            ie.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ie);
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
    }

    private String getFacebookPageURL(Context context, String facebookUrl, String pageId) {
        if (!facebookUrl.startsWith("http://www.facebook.com") && !facebookUrl.startsWith("https://www.facebook.com")) {
            return facebookUrl;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + URLEncoder.encode(facebookUrl);
            } else if (!pageId.isEmpty()) {
                return "fb://page/" + pageId;
            }
        } catch (PackageManager.NameNotFoundException e) {
            SDKUtil.logException(e);
        }
        return facebookUrl;
    }

}
