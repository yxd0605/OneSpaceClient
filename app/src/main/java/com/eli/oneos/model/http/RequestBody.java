package com.eli.oneos.model.http;

import android.nfc.Tag;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.eli.oneos.utils.CrashHandler.TAG;

/**
 * Created by eli100 on 2017/1/18.
 */


public class RequestBody {
    private String method = null;
    private String session = null;
    private Map<String, Object> params = null;

    public RequestBody(String method, String session, Map<String, Object> params) {
        this.method = method;
        this.session = session;
        this.params = params;
    }

    public JSONObject json() {
        JSONObject json = new JSONObject();
        try {
            json.put("method", method);
            json.put("session", session);
            JSONObject paramsJson = new JSONObject();
            Set set = params.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) i.next();
                Object value = entry.getValue();
                if (value instanceof ArrayList) {
                    ArrayList list = (ArrayList) value;
                    JSONArray jsonArray = new JSONArray(list);
                    paramsJson.put(entry.getKey(), jsonArray);
                } else {
                    paramsJson.put(entry.getKey(), entry.getValue());
                }
            }
            json.put("params", paramsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public String jsonString() {
        return json().toString();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}