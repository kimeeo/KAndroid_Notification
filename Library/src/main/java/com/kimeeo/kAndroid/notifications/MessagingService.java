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

import java.util.Map;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public class MessagingService extends FirebaseMessagingService {


    public static final String NOTIFICATION_BODY = "notification_body";
    public static final String NOTIFICATION_DATA = "notification_data";
    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_TITLE_LOCALIZATION_KEY = "notification_titleLocalizationKey";
    public static final String NOTIFICATION_BODY_LOCALIZATION_KEY = "notification_bodyLocalizationKey";
    public static final String NOTIFICATION_CLICK_ACTION = "notification_clickAction";
    public static final String NOTIFICATION_COLOR = "notification_color";
    public static final String NOTIFICATION_SOUND = "notification_sound";
    public static final String NOTIFICATION_ICON = "notification_icon";
    public static final String NOTIFICATION_TAG = "notification_tag";


    public static Class<Activity>  openActivity;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationReceived(remoteMessage);
    }
    protected void notificationReceived(RemoteMessage remoteMessage)
    {
        if(getApplication() instanceof NotificationApp)
        {
            NotificationApp app=(NotificationApp)getApplication();
            if(app.showDefaultMessage(remoteMessage))
            {
                Class<Activity> activity =  app.getOpenActivity(remoteMessage);
                if(activity==null)
                    activity = openActivity;
                showNotification(remoteMessage,activity);
            }
            else
                app.showNotification(remoteMessage);

            app.notificationReceived(remoteMessage);
        }
        else
            showNotification(remoteMessage,openActivity);
    }

    protected void showNotification(RemoteMessage remoteMessage, Class<Activity> activity) {

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this,activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(remoteMessage.getData()!=null)
        {
            String data="{";
            Map<String,String> map=remoteMessage.getData();
            int count=0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                data +='"'+entry.getKey()+'"'+":"+'"'+entry.getValue()+'"';
                if(count<map.entrySet().size()-2)
                    data += ",";
                count++;
            }
            data += "}";
            intent.putExtra(NOTIFICATION_DATA,data);
        }
        intent.putExtra(NOTIFICATION_BODY,notification.getBody());
        intent.putExtra(NOTIFICATION_TITLE,notification.getTitle());
        intent.putExtra(NOTIFICATION_TITLE_LOCALIZATION_KEY,notification.getTitleLocalizationKey());
        intent.putExtra(NOTIFICATION_BODY_LOCALIZATION_KEY,notification.getBodyLocalizationKey());
        intent.putExtra(NOTIFICATION_CLICK_ACTION,notification.getClickAction());
        intent.putExtra(NOTIFICATION_COLOR,notification.getColor());
        intent.putExtra(NOTIFICATION_SOUND,notification.getSound());
        intent.putExtra(NOTIFICATION_ICON,notification.getIcon());
        intent.putExtra(NOTIFICATION_TAG,notification.getTag());
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
