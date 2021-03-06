package me.iceliushuai.demo.referrer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.analytics.HitBuilders;

public class InstallReferrerReceiver {

    public static final String REFERRER_RECEIVED = BuildConfig.APPLICATION_ID + ".action.REFERRER_RECEIVED";

    public static void setup(Context context) {
        InstallReferrerClient client = InstallReferrerClient.newBuilder(context)
                .build();
        client.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {
                        ReferrerDetails details = client.getInstallReferrer();
                        String referral = details.getInstallReferrer();

                        if (!TextUtils.isEmpty(referral)) {
                            saveReferral(context, referral);
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            Intent referrerReceived = new Intent(REFERRER_RECEIVED);
                            referrerReceived.putExtra("referrer", referral);
                            lbm.sendBroadcast(referrerReceived);
                            sendAnalytics(referral);
                        }
                    } catch (RemoteException e) {
                        // omit exception
                    }
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {

            }
        });
    }

    /**
     * Send GA Campaign Analytics
     *
     * @param referrer
     */
    private static void sendAnalytics(String referrer) {
        AnalyticsTrackers.getAppTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Install")
                .setAction("referrer_count")
                .setLabel(referrer)
                .setValue(1)
                .build());
    }

    public static void saveReferral(Context context, String referrer) {
        SharedPreferences prefs = context.getSharedPreferences("referrer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("referrer", referrer);
        editor.apply();
    }

    public static String getSavedReferral(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("referrer", Context.MODE_PRIVATE);
        return prefs.getString("referrer", null);
    }
}
