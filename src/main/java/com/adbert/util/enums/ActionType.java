package com.adbert.util.enums;

public enum ActionType {
    act_web(0), act_video2(1);

    private int code;

    ActionType(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
