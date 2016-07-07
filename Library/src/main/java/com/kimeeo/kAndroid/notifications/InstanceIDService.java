package com.kimeeo.kAndroid.notifications;

import android.provider.Settings;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.TimeZone;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public class InstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String uuid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        notificationRegistration(refreshedToken,uuid, TimeZone.getDefault());
    }

    public void notificationRegistration(String token,String uuid,TimeZone time)
    {
        if(getApplication() instanceof NotificationApp)
            ((NotificationApp)getApplication()).notificationRegistration(token,uuid,time);
    }
}