package org.sunbird.utm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;



public class ReferrerReceiver extends BroadcastReceiver {
    public final static String PREFS_FILE_NAME = "UtmInfo";
    public final static String[] EXPECTED_PARAMETERS = {"utm_source",
            "utm_medium", "utm_term", "utm_content", "utm_campaign"};

    @Override
    public void onReceive(Context context, Intent intent) {

        JSONObject referrerInfo = new JSONObject();
        if (intent == null) {
            return;
        }
        if (!intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
            return;
        }

        String referrer = intent.getStringExtra("referrer");
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }
        try {
            String[] params = referrer.split("&");

            for (String param : params) {
                String[] pair = param.split("=");

                if (pair.length == 1) {
                    referrerInfo.put(pair[0], "app");
                } else if (pair.length == 2) {
                    referrerInfo.put(pair[0], pair[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences storage = context.getSharedPreferences(
                ReferrerReceiver.PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString("utm_data", referrerInfo.toString());
        editor.commit();


    }

}


