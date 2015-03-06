package com.groupit;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

    public static String getJSONMessage(String sessionId, String message) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("sessionId", sessionId);
            jObj.put("message", message);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String getMessage(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("message");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getID(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("sessionId");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
 }
