package com.kimeeo.kAndroid.notifications;

import android.app.Activity;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public interface NotificationApp {
    void notificationRegistration(String token,String uuid);
    boolean showDefaultMessage(RemoteMessage.Notification notification);
    void notificationReceived(RemoteMessage.Notification notification);
    Class<Activity> getOpenActivity(RemoteMessage.Notification notification);
    void showNotification(RemoteMessage.Notification notification);
    void sendNotification(Notification notification);
}