package org.sunbird.config;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IntentUtil {

    public static Intent populateIntent(JSONObject obj, CallbackContext callbackContext) throws JSONException {
        String type = obj.has("type") ? obj.getString("type") : null;
        String packageAssociated = obj.has("package") ? obj.getString("package") : null;

        Uri uri = null;
        if (obj.has("url")) {
            String uriAsString = obj.getString("url");
        }

        JSONObject extras = obj.has("extras") ? obj.getJSONObject("extras") : null;
        Map<String, Object> extrasMap = new HashMap<String, Object>();
        Bundle bundle = null;
        String bundleKey = "";
        if (extras != null) {
            JSONArray extraNames = extras.names();
            for (int i = 0; i < extraNames.length(); i++) {
                String key = extraNames.getString(i);
                Object extrasObj = extras.get(key);
                if (extrasObj instanceof JSONObject) {
                    //  The extra is a bundle
                    bundleKey = key;
                    bundle = toBundle((JSONObject) extras.get(key));
                } else {
                    extrasMap.put(key, extras.get(key));
                }
            }
        }

        String action = obj.has("action") ? obj.getString("action") : null;
        Intent i = new Intent();
        if (action != null)
            i.setAction(action);

        if (type != null && uri != null) {
            i.setDataAndType(uri, type); //Fix the crash problem with android 2.3.6
        } else {
            if (type != null) {
                i.setType(type);
            }
            if (uri != null) {
                i.setData(uri);
            }
        }

        JSONObject component = obj.has("component") ? obj.getJSONObject("component") : null;
        if (component != null) {
            //  User has specified an explicit intent
            String componentPackage = component.has("package") ? component.getString("package") : null;
            String componentClass = component.has("class") ? component.getString("class") : null;
            if (componentPackage == null || componentClass == null) {
                Log.w("UtilityClassPlugin", "Component specified but missing corresponding package or class");
                throw new JSONException("Component specified but missing corresponding package or class");
            } else {
                ComponentName componentName = new ComponentName(componentPackage, componentClass);
                i.setComponent(componentName);
            }
        }

        if (packageAssociated != null)
            i.setPackage(packageAssociated);



        if (bundle != null)
            i.putExtra(bundleKey, bundle);

        for (String key : extrasMap.keySet()) {
            Object value = extrasMap.get(key);
            String valueStr = String.valueOf(value);

            if (value instanceof Boolean) {
                i.putExtra(key, Boolean.valueOf(valueStr));
            } else if (value instanceof Integer) {
                i.putExtra(key, Integer.valueOf(valueStr));
            } else if (value instanceof Long) {
                i.putExtra(key, Long.valueOf(valueStr));
            } else if (value instanceof Double) {
                i.putExtra(key, Double.valueOf(valueStr));
            } else {
                i.putExtra(key, valueStr);
            }

        }
        return i;
    }


    private static Bundle toBundle(final JSONObject obj) {
        Bundle returnBundle = new Bundle();
        if (obj == null) {
            return null;
        }
        try {
            Iterator<?> keys = obj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object compare = obj.get(key);
                if (obj.get(key) instanceof String)
                    returnBundle.putString(key, obj.getString(key));
                else if (obj.get(key) instanceof Boolean)
                    returnBundle.putBoolean(key, obj.getBoolean(key));
                else if (obj.get(key) instanceof Integer)
                    returnBundle.putInt(key, obj.getInt(key));
                else if (obj.get(key) instanceof Long)
                    returnBundle.putLong(key, obj.getLong(key));
                else if (obj.get(key) instanceof Double)
                    returnBundle.putDouble(key, obj.getDouble(key));
                else if (obj.get(key).getClass().isArray() || obj.get(key) instanceof JSONArray) {
                    JSONArray jsonArray = obj.getJSONArray(key);
                    int length = jsonArray.length();
                    if (jsonArray.get(0) instanceof String) {
                        String[] stringArray = new String[length];
                        for (int j = 0; j < length; j++)
                            stringArray[j] = jsonArray.getString(j);
                        returnBundle.putStringArray(key, stringArray);
                        //returnBundle.putParcelableArray(key, obj.get);
                    } else {
                        if (key.equals("PLUGIN_CONFIG")) {
                            ArrayList<Bundle> bundleArray = new ArrayList<Bundle>();
                            for (int k = 0; k < length; k++) {
                                bundleArray.add(toBundle(jsonArray.getJSONObject(k)));
                            }
                            returnBundle.putParcelableArrayList(key, bundleArray);
                        } else {
                            Bundle[] bundleArray = new Bundle[length];
                            for (int k = 0; k < length; k++)
                                bundleArray[k] = toBundle(jsonArray.getJSONObject(k));
                            returnBundle.putParcelableArray(key, bundleArray);
                        }
                    }
                } else if (obj.get(key) instanceof JSONObject)
                    returnBundle.putBundle(key, toBundle((JSONObject) obj.get(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return returnBundle;
    }

    public static JSONObject getIntentJson(Intent intent) {
        JSONObject intentJSON = null;
        try {
            intentJSON = new JSONObject();
            intentJSON.put("type", intent.getType());
            intentJSON.put("extras", toJsonObject(intent.getExtras()));
            intentJSON.put("action", intent.getAction());
            intentJSON.put("categories", intent.getCategories());
            intentJSON.put("flags", intent.getFlags());
            intentJSON.put("component", intent.getComponent());
            intentJSON.put("data", intent.getData());
            intentJSON.put("package", intent.getPackage());
            return intentJSON;
        } catch (JSONException e) {
            Log.d("UtilityPlugin", " Error thrown during intent > JSON conversion");
            Log.d("UtilityPlugin", e.getMessage());
            Log.d("UtilityPlugin", Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    private static JSONObject toJsonObject(Bundle bundle) {
        //  Credit: https://github.com/napolitano/cordova-plugin-intent
        try {
            return (JSONObject) toJsonValue(bundle);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Cannot convert bundle to JSON: " + e.getMessage(), e);
        }
    }

    private static Object toJsonValue(final Object value) throws JSONException {
        //  Credit: https://github.com/napolitano/cordova-plugin-intent
        if (value == null) {
            return null;
        } else if (value instanceof Bundle) {
            final Bundle bundle = (Bundle) value;
            final JSONObject result = new JSONObject();
            for (final String key : bundle.keySet()) {
                result.put(key, toJsonValue(bundle.get(key)));
            }
            return result;
        } else if ((value.getClass().isArray())) {
            final JSONArray result = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                result.put(i, toJsonValue(Array.get(value, i)));
            }
            return result;
        } else if (value instanceof ArrayList<?>) {
            final ArrayList arrayList = (ArrayList<?>) value;
            final JSONArray result = new JSONArray();
            for (int i = 0; i < arrayList.size(); i++)
                result.put(toJsonValue(arrayList.get(i)));
            return result;
        } else if (
                value instanceof String
                        || value instanceof Boolean
                        || value instanceof Integer
                        || value instanceof Long
                        || value instanceof Double) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }


}
