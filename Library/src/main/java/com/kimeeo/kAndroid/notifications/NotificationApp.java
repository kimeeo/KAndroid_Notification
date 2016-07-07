package com.kimeeo.kAndroid.notifications;

import android.app.Activity;

import com.google.firebase.messaging.RemoteMessage;

import java.util.TimeZone;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public interface NotificationApp {
    void notificationRegistration(String token, String deviceUUID, TimeZone time);
    boolean showDefaultMessage(RemoteMessage remoteMessage);
    void notificationReceived(RemoteMessage remoteMessage);
    Class<Activity> getOpenActivity(RemoteMessage remoteMessage);
    void showNotification(RemoteMessage remoteMessage);
    void sendNotification(Notification remoteMessage);
}