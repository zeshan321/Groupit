package com.groupit;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

    public static String getJSONMessage(String ID, String group, String message, String display) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("ID", ID);
            jObj.put("group", String.valueOf(group));
            jObj.put("message", message);
            jObj.put("display", display);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static Boolean canUseMessage(String json) {
        try {
            JSONObject jObj = new JSONObject(json);

            if (jObj.isNull("ID") == false && jObj.isNull("group") == false && jObj.isNull("message") == false && jObj.isNull("display") == false) {
                return  true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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
            String msg = jObj.getString("ID");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getName(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("display");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Groups
    public static String getJSOnGroup(String display, String group) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("display", display);
            jObj.put("group", group);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String getGroupDisplay(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("display");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getGroupID(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("group");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getJSONList() {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("listofgroups", GroupActivity.groups.toString());

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
 }
