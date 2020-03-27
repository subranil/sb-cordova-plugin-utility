package org.sunbird.utm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

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
                    reffererMap.put("val", response.getInstallReferrer());
                }

                reffererMap.put("clk", Long.toString(response.getReferrerClickTimestampSeconds()));
                reffererMap.put("install", Long.toString(response.getInstallBeginTimestampSeconds()));
            }

            if (this.mInstallReferrerListener != null) {
                this.mInstallReferrerListener.onHandlerReferrer(reffererMap);
            }

        }

    }
}
