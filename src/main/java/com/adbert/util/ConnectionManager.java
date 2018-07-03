package com.adbert.util;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by chihhan on 2017/7/4.
 */

public class ConnectionManager {
    private static ConnectionManager ourInstance = new ConnectionManager();

    public static ConnectionManager getInstance() {
        return ourInstance;
    }

    private ConnectionManager() {

    }

    public interface ConnectionListener {
        void onConnectionSuccess(CustomConnection cc);

        void onConnectionFail(CustomConnection cc);
    }

    public interface SimpleConnectionListener {
        void onEnd(int code, String result);
    }

    public interface DownloadListener {
        void onDownloadSuccess(Bitmap bitmap, String soureUrl, String cacheUrl);

        void onDownloadFail();
    }

    public interface MultiConnectionListener extends ConnectionListener {

        void onTimeOut(CustomConnection cc);

        void onException(CustomConnection cc);
    }

    /**
     * 建立新的連線物件
     *
     * @param context Context
     */
    public CustomConnection newConnection(Context context) {
        return new CustomConnection(context);
    }

    /**
     * 建立新的連線物件(POST)，不管連線結果
     *
     * @param context Context
     * @param url     連線網址
     * @param data    POST資料
     */
    public CustomConnection newSimpleConnection(Context context, String url, String data) {
        return newSimpleConnection(context, url, data, null);
    }

    /**
     * 建立新的連線物件(POST)，直接回收、不管狀態
     *
     * @param context Context
     * @param url     連線網址
     * @param data    POST資料
     */
    public CustomConnection newSimpleConnection(Context context, String url, String data, final SimpleConnectionListener listener) {
        CustomConnection connection = new CustomConnection(context);
        if (listener != null) {
            connection.setListener(new ConnectionListener() {
                @Override
                public void onConnectionSuccess(CustomConnection cc) {
                    listener.onEnd(cc.getResponseCode(), cc.getResult());
                }

                @Override
                public void onConnectionFail(CustomConnection cc) {
                    listener.onEnd(cc.getResponseCode(), cc.getResult());
                }
            });
        }
        connection.post(url, data);
        return connection;
    }

}
