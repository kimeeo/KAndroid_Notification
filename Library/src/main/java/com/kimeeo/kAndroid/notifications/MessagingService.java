package com.kimeeo.kAndroid.notifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public class MessagingService extends FirebaseMessagingService {

    public static final String BODY = "body";
    public static final String TITLE = "title";
    public static final String TITLE_LOCALIZATION_KEY = "titleLocalizationKey";
    public static final String BODY_LOCALIZATION_KEY = "bodyLocalizationKey";
    public static final String CLICK_ACTION = "clickAction";
    public static final String COLOR = "color";
    public static final String SOUND = "sound";
    public static final String ICON = "icon";
    public static final String TAG = "tag";


    public static Class<Activity>  openActivity;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationReceived(remoteMessage.getNotification());
    }
    protected void notificationReceived(RemoteMessage.Notification notification)
    {
        if(getApplication() instanceof NotificationApp)
        {
            NotificationApp app=(NotificationApp)getApplication();
            if(app.showDefaultMessage(notification))
            {
                Class<Activity> activity =  app.getOpenActivity(notification);
                if(activity==null)
                    activity = openActivity;
                showNotification(notification,activity);
            }
            else
                app.showNotification(notification);

            app.notificationReceived(notification);
        }
        else
            showNotification(notification,openActivity);
    }

    protected void showNotification(RemoteMessage.Notification notification, Class<Activity> activity) {
        Intent intent = new Intent(this,activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String body = notification.getBody();
        String title = notification.getTitle();
        String titleLocalizationKey = notification.getTitleLocalizationKey();
        String bodyLocalizationKey = notification.getBodyLocalizationKey();
        String clickAction = notification.getClickAction();
        String color = notification.getColor();
        String sound = notification.getSound();
        String icon = notification.getIcon();
        String tag = notification.getTag();
        intent.putExtra(BODY,body);
        intent.putExtra(TITLE,title);
        intent.putExtra(TITLE_LOCALIZATION_KEY,titleLocalizationKey);
        intent.putExtra(BODY_LOCALIZATION_KEY,bodyLocalizationKey);
        intent.putExtra(CLICK_ACTION,clickAction);
        intent.putExtra(COLOR,color);
        intent.putExtra(SOUND,sound);
        intent.putExtra(ICON,icon);
        intent.putExtra(TAG,tag);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(getActivityIcon(getPackageName()))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    public Bitmap getActivityIcon(String packageName) {

        try
        {
            Drawable drawable=getPackageManager().getApplicationIcon(packageName);
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if(bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }catch (Exception e)
        {

        }
        return null;
    }
}
