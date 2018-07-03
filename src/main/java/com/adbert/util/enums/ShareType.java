package com.adbert.util.enums;

public enum ShareType {
    init(-1), download(0), url(1), phone(2), fb(3), line(4);

    private int code;

    ShareType(int code) {
        this.code = code;
    }

    public int getIdx() {
        return code;
    }

    public String fromInt(int i) {
        for (ShareType b : ShareType.values()) {
            if (b.getIdx() == i) {
                return b.toString();
            }
        }
        return null;
    }

    public ShareType getTypeFromPosition(int i) {
        for (ShareType b : ShareType.values()) {
            if (b.getIdx() == i) {
                return b;
            }
        }
        return this;
    }
}
