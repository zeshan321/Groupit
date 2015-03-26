package com.groupit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONUtils {

    public String getJSONMessage(String ID, String group, String message, String display, boolean isImage) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("ID", ID);
            jObj.put("group", String.valueOf(group));
            jObj.put("message", message);
            jObj.put("display", display);
            jObj.put("image", isImage);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public String nfcGroup(String display, String ID) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("display", display);
            jObj.put("ID", ID);

            json = jObj.toString();

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String nfcGetDisplay(String json) {
        String display = null;
        try {
            JSONObject jObj = new JSONObject(json);
            display = jObj.getString("display");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return display;
    }

    public String nfcGetID(String json) {
        String display = null;
        try {
            JSONObject jObj = new JSONObject(json);
            display = jObj.getString("ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return display;
    }

    public  Boolean isImage(String json) {
        try {
            JSONObject jObj = new JSONObject(json);

            if (jObj.isNull("image")) {
                return false;
            }

            if (jObj.getBoolean("image")) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean canUseMessage(String json) {
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

    public String getMessage(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("message");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getID(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("ID");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName(String json) {
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
    public String getJSOnGroup(String display, String group) {
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

    public String getGroupDisplay(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("display");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getGroupID(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            String msg = jObj.getString("group");
            return msg;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getJSONList() {
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
