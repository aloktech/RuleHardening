/**
 * ***********************************************************************
 * INVICARA INC CONFIDENTIAL
 * ***********************************************************************
 *
 * Copyright (C) [2012] - [2014] INVICARA INC, INVICARA Pte Ltd, INVICARA INDIA
 * PVT LTD All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Invicara Inc and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to Invicara Inc and its suppliers
 * and may be covered by U.S. and Foreign Patents, patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Invicara Inc.
 */
package com.invicara.tools.rh.utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alok Ranjan Meher
 */
public class RulesHardeningHelper {

    public static String getTimeAsName() {
        Calendar calendar = GregorianCalendar.getInstance();
        StringBuilder builder = new StringBuilder();
        int value;
        builder.append(new DateFormatSymbols().getShortMonths()[calendar.get(Calendar.MONTH)]);
        builder.append("_");
        value = calendar.get(Calendar.DAY_OF_MONTH);
        builder.append(getValueAsString(value));
        builder.append("_");
        value = calendar.get(Calendar.HOUR_OF_DAY);
        builder.append(getValueAsString(value));
        value = calendar.get(Calendar.MINUTE);
        builder.append(getValueAsString(value));
        value = calendar.get(Calendar.SECOND);
        builder.append(getValueAsString(value));
        return builder.toString();
    }

    private static Object getValueAsString(int value) {
        return value < 10 ? "0" + value : value;
    }

    public static String emptyTheJSON(String json) {
        if (json.trim().startsWith("{")) {
            JSONObject root = parseJSONObject(new JSONObject(json));
             return root.toString();
        } else  if (json.trim().startsWith("[")) {
            JSONArray root = new JSONArray(json);
            for (int index = 0; index < root.length(); index++) {
                JSONObject obj = root.getJSONObject(index);
                obj = parseJSONObject(obj);
                root.put(index, obj);
            }
            root.toString();
        }

        return json;
    }

    private static JSONObject parseJSONObject(JSONObject root) throws JSONException {
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = root.get(key);
            if (obj instanceof String) {
                root.put(key, "");
            } else if (obj instanceof Integer || obj instanceof Double) {
                root.put(key, 0);
            } else if (obj instanceof JSONObject) {
                JSONObject troot = (JSONObject) obj;
                String data = emptyTheJSON(troot.toString());
            } else if (obj instanceof JSONArray) {
                root.put(key, new JSONArray());
            }
        }
        return root;
    }
}
