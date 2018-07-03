package com.adbert.util;

/**
 * Created by chihhan on 16/8/2.
 */
public class TestMode {
    public static String testUrl = "";
    public static ReturnLogListener logListener;

    public interface ReturnLogListener {
        void returnLog(String msg);
    }
}
