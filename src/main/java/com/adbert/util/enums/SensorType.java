package com.adbert.util.enums;

import android.hardware.Sensor;

public enum SensorType {
    LIGHT(Sensor.TYPE_LIGHT, 0), SHAKE(Sensor.TYPE_ACCELEROMETER, 1),
    MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD, 2), DISTANCE(Sensor.TYPE_PROXIMITY, 3),
    GYROSCOPE(Sensor.TYPE_GYROSCOPE, 4), PRESSURE(Sensor.TYPE_PRESSURE, 5),
    GRAVITY(Sensor.TYPE_GRAVITY, 6);

    private int code;
    private int value;

    SensorType(int code, int value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public int fromInt(int i) {
        for (SensorType b : SensorType.values()) {
            if (b.getValue() == i) {
                return b.getCode();
            }
        }
        return -1;
    }
}
