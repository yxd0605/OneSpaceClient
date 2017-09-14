package com.eli.oneos.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class GsonUtils {

    public static String encodeJSON(Object src) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.toJson(src);
    }

    public static <T> T decodeJSON(String jsonString, Class<T> cls) throws JsonSyntaxException {
        Gson gson = new Gson();
        T model = gson.fromJson(jsonString, cls);
        return model;
    }

    public static <T> T decodeJSON(String jsonString, Type typeOfT) throws JsonSyntaxException {
        if (jsonString.equals("{}")) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(jsonString, typeOfT);
    }

    public static Map<String, Object> decodeJSONToMap(String jsonString) throws JsonSyntaxException {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
        }.getType());
        return map;
    }
}
