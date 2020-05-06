package org.sunbird.utm;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class PlayStoreInstallReferrer implements InstallReferrerStateListener {

    private InstallReferrerClient mReferrerClient;
    private InstallReferrerListener mInstallReferrerListener;

    public void start(Context context, InstallReferrerListener installReferrerListener) {
        this.mInstallReferrerListener = installReferrerListener;
        this.mReferrerClient = InstallReferrerClient.newBuilder(context).build();

        try {
            this.mReferrerClient.startConnection(this);
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.e("startConnection error: ", exception.getMessage());
        }
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        ReferrerDetails response = null;
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                // Connection established.
                try {
                    response = mReferrerClient.getInstallReferrer();
                    String referrerUrl = response.getInstallReferrer();
                    long referrerClickTime = response.getReferrerClickTimestampSeconds();
                    long appInstallTime = response.getInstallBeginTimestampSeconds();
                    boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
                    Log.e("InstallReferrer referrerUrl: ", referrerUrl);
                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                }
                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app.
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                // Connection couldn't be established.
                break;
        }
        this.handleReferrer(response, responseCode);

    }

    @Override
    public void onInstallReferrerServiceDisconnected() {

    }

    private void handleReferrer(@Nullable ReferrerDetails response, int responseCode) {
        Map reffererMap = new HashMap();
        if (!response.getInstallReferrer().contains("google-play")) {
            reffererMap.put("code", String.valueOf(responseCode));
            if (response != null) {
                if (response.getInstallReferrer() != null) {
                    try {
                        reffererMap.put("val", this.splitQuery(new URL("https://mock.com?" + response.getInstallReferrer())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                reffererMap.put("clk", Long.toString(response.getReferrerClickTimestampSeconds()));
                reffererMap.put("install", Long.toString(response.getInstallBeginTimestampSeconds()));
            }

            if (this.mInstallReferrerListener != null) {
                this.mInstallReferrerListener.onHandlerReferrer(reffererMap);
            }

        }

    }

    public JSONArray splitQuery(URL url) throws UnsupportedEncodingException {
        JSONArray campaignParams = new JSONArray();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            JSONObject campaignObject = new JSONObject();
            int idx = pair.indexOf("=");

            String name = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
            String channelId = (Uri.parse(value)).getQueryParameter("channel");
            if (channelId != null) {
                try {
                    JSONObject campaignChannelObject = new JSONObject();
                    campaignChannelObject.put("id", channelId);
                    campaignChannelObject.put("type", "Source");
                    campaignParams.put(campaignChannelObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (name.equals("utm_campaign") || name.equals("channel")) {
                try {
                    campaignObject.put("id", value);
                    campaignObject.put("type", "Source");
                    campaignParams.put(campaignObject);
                } catch (Exception e) {
                }

            } else {
                if (name.contains("_")) {
                    int i = name.indexOf("_");
                    name = name.replace("_", "");
                    name = name.substring(0, 1).toUpperCase() + name.substring(1, i) + name.substring(i, i + 1).toUpperCase() + name.substring(i + 1);
                }
                try {
                    campaignObject.put("id", value);
                    campaignObject.put("type", name);
                    campaignParams.put(campaignObject);
                } catch (Exception e) {
                }
            }
        }

        return campaignParams;
    }
}