package com.adbert.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

/**
 * Created by chihhan on 2017/7/4.
 */

public class CustomConnection {
    private boolean log = SDKUtil.log;
    private int loaderId = 0;
    private ConnectionManager.ConnectionListener listener;
    private Context context;
    private int responseCode = 0;
    private String result = "";
    private int TIMEOUT = 10000;
    private Bitmap bitmap;
    private String finalUrl = "";

    public CustomConnection(Context context) {
        this.context = context;
    }

    public CustomConnection setLoaderId(int loaderId) {
        this.loaderId = loaderId;
        return this;
    }

    public int getLoaderId() {
        return loaderId;
    }

    public CustomConnection setListener(ConnectionManager.ConnectionListener listener) {
        this.listener = listener;
        return this;
    }

    public ConnectionManager.ConnectionListener getListener() {
        return listener;
    }

    private void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    private void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setUrl(String url) {
        this.finalUrl = url;
    }

    public void recycleBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public void post(String url, Map<String, String> map) {
        post(url, getMapString(map));
    }

    public void post(String url, String param) {
        if (log) {
            if (TestMode.logListener != null) {
                TestMode.logListener.returnLog(url + "?" + param);
            }
            SDKUtil.logTestMsg(url + "?" + param);
        }
        ConnectionData data = new ConnectionData(ConnectionType.post);
        data.setUrl(url);
        data.setParam(param);
        commonConnection(data);
    }

    private void commonConnection(final ConnectionData data) {
        if ((data.getUrl().isEmpty() || data.getUrl().startsWith("?") || !isConnectable(context)) && listener != null) {
            listener.onConnectionFail(this);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection conn = null;
                    DataOutputStream dos = null;
                    BufferedReader reader = null;
                    InputStream is = null;
                    try {
                        String url = data.getUrl();
                        URL uri;
                        URL base, next;
                        while (true) {
                            uri = new URL(url);
                            conn = setting(uri, data);
                            conn.connect();
                            output(conn, data);
                            int status = conn.getResponseCode();
                            if (status != HttpURLConnection.HTTP_OK) {
                                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                        || status == HttpURLConnection.HTTP_MOVED_PERM
                                        || status == HttpURLConnection.HTTP_SEE_OTHER
                                        || status == 307) {
                                    base = new URL(url);
                                    next = new URL(base, conn.getHeaderField("Location"));  // Deal with relative URLs
                                    url = next.toExternalForm();
                                    continue;
                                }
                            }
                            break;
                        }
                        setUrl(url);
                        setResponseCode(conn.getResponseCode());
                        input(uri, conn, data);
                    } catch (SocketTimeoutException e) {
                        setResponseCode(-1001);
                        if (log) e.printStackTrace();
                    } catch (Exception e) {
                        if (log) e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                        try {
                            if (dos != null) {
                                dos.flush();
                                dos.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            if (log) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //callback
                    if (getListener() != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                int responseCode = getResponseCode();
                                if (responseCode == 200) {
                                    getListener().onConnectionSuccess(CustomConnection.this);
                                } else if (getListener() instanceof ConnectionManager.MultiConnectionListener) {
                                    ConnectionManager.MultiConnectionListener listener = ((ConnectionManager.MultiConnectionListener) getListener());
                                    if (responseCode == -1001) {
                                        listener.onTimeOut(CustomConnection.this);
                                    } else {
                                        listener.onException(CustomConnection.this);
                                    }
                                } else {
                                    getListener().onConnectionFail(CustomConnection.this);
                                }
                            }
                        });
                    }

                    if (data.getType() == ConnectionType.post && log) {
                        SDKUtil.logTestMsg("responseCode = " + getResponseCode() + "\nresult = " + getResult());
                    }

                }
            }).start();
        }
    }

    private HttpURLConnection setting(URL uri, ConnectionData data) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoInput(true);
        if (data.getType() == ConnectionType.post) {
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(data.getParamByte().length));
        } else if (data.getType() == ConnectionType.postWithImage) {
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", "photo.jpg");
        } else if (data.getType() == ConnectionType.gets) {
            conn.setRequestMethod("GET");
        }
//        else if (data.getType() == ConnectionType.getImage) {
//             conn.setRequestProperty("User-Agent", "Mozilla");
//        }
        return conn;
    }

    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";

    private void output(HttpURLConnection conn, ConnectionData data) throws Exception {
        if (data.getType() == ConnectionType.post) {
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(data.getParamByte());
        } else if (data.getType() == ConnectionType.postWithImage) {
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + data.getPostPhotoKey() + "\";filename=" + "photo.jpg" + "" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.write(data.getPhoto());
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            if (data.getMap() != null && data.getMap().size() > 0) {
                for (Map.Entry<String, String> pair : data.getMap().entrySet()) {
                    String key = pair.getKey();
                    String value = pair.getValue();
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    byte[] b = value.getBytes("UTF-8");
                    dos.write(b);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                }
            }
        }
    }

    private void input(URL uri, HttpURLConnection conn, ConnectionData data) throws Exception {
        if (data.getType() == ConnectionType.postWithImage || data.getType() == ConnectionType.post || data.getType() == ConnectionType.gets) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String str = "";
            String lines;
            while ((lines = reader.readLine()) != null) {
                str += lines;
            }
            setResult(str);
        } else if (data.getType() == ConnectionType.getImage) {
            InputStream is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                setBitmap(bitmap);
            } else {
                responseCode = 0;
            }
        } else if (data.getType() == ConnectionType.getImageAndSave) {
            InputStream is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                setBitmap(bitmap);
                savePic(bitmap, data.getSavePath());
                if (!new File(data.getSavePath()).exists()) {
                    responseCode = 0;
                }
            } else {
                responseCode = 0;
            }
        } else if (data.getType() == ConnectionType.getFile) {
            InputStream is;//= conn.getInputStream();
            if (!new File(data.getSavePath()).exists()) { //use cache
                is = uri.openStream();
                File f = new File(data.getSavePath());
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buffer = new byte[1024];
                int len1 = 0;
                if (is != null) {
                    while ((len1 = is.read(buffer)) > 0)
                        fos.write(buffer, 0, len1);
                }
                if (fos != null) {
                    fos.close();
                }
                if (!new File(data.getSavePath()).exists()) {
                    responseCode = 0;
                }
            }
        }
    }

    public void postWithImage(String url, Map<String, String> map, String photoKey, byte[] buffer) {
        ConnectionData data = new ConnectionData(ConnectionType.postWithImage);
        data.setUrl(url);
        data.setMap(map);
        data.setPostPhotoKey(photoKey);
        data.setPhoto(buffer);
        commonConnection(data);
    }

    public void getImage(String url) {
        ConnectionData data = new ConnectionData(ConnectionType.getImage);
        data.setUrl(url);
        commonConnection(data);
    }

    private void getImageAndSave(String url, String savePath) {
        ConnectionData data = new ConnectionData(ConnectionType.getImageAndSave);
        data.setUrl(url);
        data.setSavePath(savePath);
        commonConnection(data);
    }

    public void getFileAndSave(String url, String savePath) {
        ConnectionData data = new ConnectionData(ConnectionType.getFile);
        data.setUrl(url);
        data.setSavePath(savePath);
        commonConnection(data);
    }

    public void get(String url) {
        ConnectionData data = new ConnectionData(ConnectionType.gets);
        data.setUrl(url);
        commonConnection(data);
    }

    //---------------Tool--------------

    private String getMapString(Map<String, String> map) {
        String param = "";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!param.isEmpty()) {
                param += "&";
            }
            param += key + "=" + value;
        }
        return param;
    }

    private String getBundleString(Bundle bundle) {
        String string = "";
        for (String key : bundle.keySet()) {
            if (string.isEmpty()) {
                string += key + " = " + bundle.get(key);
            } else {
                string += "&" + key + "=" + bundle.get(key);
            }
        }
        return string;
    }

    private boolean isConnectable(Context context) {
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

    //----------------Cookie Pool--------------------------------------------

    private boolean cookieEnable = true;

    //-------------------Data

    private enum ConnectionType {
        post, gets, postWithImage, getImage, getImageAndSave, getFile
    }

    private class ConnectionData {
        private ConnectionType type;
        private String url = "";
        private String param = "";
        private Map<String, String> map = null;
        private String postPhotoKey = "";
        private byte[] photo = null;
        private String savePath = "";

        public ConnectionData(ConnectionType type) {
            this.type = type;
        }

        public ConnectionType getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

//        public String getParam() {
//            return param;
//        }

        public void setParam(String param) {
            this.param = param;
        }

        public byte[] getParamByte() throws Exception {
            return param.getBytes("UTF-8");
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        public String getPostPhotoKey() {
            return postPhotoKey;
        }

        public void setPostPhotoKey(String postPhotoKey) {
            this.postPhotoKey = postPhotoKey;
        }

        public byte[] getPhoto() {
            return photo;
        }

        public void setPhoto(byte[] photo) {
            this.photo = photo;
        }

        public String getSavePath() {
            return savePath;
        }

        public void setSavePath(String savePath) {
            this.savePath = savePath;
        }
    }

    private boolean savePic(Bitmap b, String filePath) {
        boolean isJPG = isGIF(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            if (null != fos) {
                if (isJPG)
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                else
                    b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            return false;
        }
        if (new File(filePath).exists())
            return true;
        return false;
    }

    private static boolean isGIF(String path) {
        if (!path.isEmpty() && (path.endsWith(".gif") || path.contains(".gif?"))) {
            return true;
        }
        return false;
    }
}
