package com.adbert;

public enum AdbertOrientation {
    LAND(0), PORT(1), NORMAL(2);

    private int code;

    AdbertOrientation(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
