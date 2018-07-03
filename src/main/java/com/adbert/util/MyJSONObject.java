package com.adbert.util;

import com.adbert.util.enums.JSONKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chihhan on 2017/9/22.
 */

public class MyJSONObject {

    JSONObject object;

    public MyJSONObject(String json) throws JSONException {
        object = new JSONObject(json);
    }

    public String getString(JSONKey name) throws JSONException {
        if (!object.has(name.toString())) {
            return "";
        }
        return object.getString(name.toString()).trim();
    }

    public boolean has(JSONKey name) {
        return object.has(name.toString());
    }

    public boolean getBoolean(JSONKey name) throws JSONException {
        if (!object.has(name.toString())) {
            return false;
        }
        return object.getBoolean(name.toString());
    }

    public JSONArray getJSONArray(JSONKey name) throws JSONException {
        if (!object.has(name.toString())) {
            return new JSONArray();
        }

        return object.getJSONArray(name.toString());
    }


}
