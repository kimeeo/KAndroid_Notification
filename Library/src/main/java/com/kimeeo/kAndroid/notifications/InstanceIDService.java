package com.kimeeo.kAndroid.notifications;

import android.provider.Settings;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public class InstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String uuid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        sendRegistrationToServer(refreshedToken,uuid);
    }

    public void sendRegistrationToServer(String token,String uuid)
    {
        if(getApplication() instanceof NotificationApp)
            ((NotificationApp)getApplication()).notificationRegistration(token,uuid);
    }
}