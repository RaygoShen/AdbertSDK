package com.adbert.util.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chihhan on 2017/9/22.
 */

public class BeaconData {

    public BeaconData() {
        updateTime();
    }

    public void updateTime() {
        String time = createTime();
        if (startTime.isEmpty()) {
            startTime = time;
        }else{
            endTime = time;
        }
    }

    private String createTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }

    private String uuid = "";
    private String major = "";
    private String minor = "";
    private String startTime = "";
    private String endTime = "";

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }


    public JSONObject getJSON(String aaid) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("uuid", aaid);
        object.put("id", uuid);
        object.put("major", major);
        object.put("minor", minor);
        object.put("startTime", startTime);
        object.put("endTime", endTime);
        return object;
    }


}
