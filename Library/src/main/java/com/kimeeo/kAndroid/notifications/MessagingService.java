package com.kimeeo.kAndroid.notifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by BhavinPadhiyar on 06/07/16.
 */
public class MessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_STYLE = "style";
    public static final String NOTIFICATION_SUMMARY_TEXT = "summaryText";
    public static final String NOTIFICATION_BIG_TEXT = "bigText";
    public static final String NOTIFICATION_BIG_CONTENT_TITLE = "bigContentTitle";
    public static final String NOTIFICATION_AUTO_CANCEL = "autoCancel";
    public static final String NOTIFICATION_IMAGE = "image";
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
    public static final int STYLE_NORMAL = 1;
    public static final int STYLE_BIG_TEXT_STYLE = 2;
    public static final int STYLE_INBOX_STYLE = 3;
    public static final int STYLE_BIG_PICTURE_STYLE = 4;


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

        Intent intent=null;
        if(activity==null)
        {
            intent = new Intent(Intent.ACTION_MAIN);
            //intent.setComponent(new ComponentName(getPackageName(),this));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            //PackageManager manager = getPackageManager();
            //intent = manager.getLaunchIntentForPackage(classPath);

        }
        else
            intent = new Intent(this,activity);

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        int style=STYLE_NORMAL;
        Map<String,String> dataMap=null;
        if(remoteMessage.getData()!=null)
        {
            dataMap=remoteMessage.getData();
            String data="{";

            int count=0;
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                data +='"'+entry.getKey()+'"'+":"+'"'+entry.getValue()+'"';
                if(count<dataMap.entrySet().size()-2)
                    data += ",";
                count++;
            }
            data += "}";
            intent.putExtra(NOTIFICATION_DATA,data);
            if(dataMap.get(NOTIFICATION_STYLE)!=null)
                style= Integer.parseInt(dataMap.get(NOTIFICATION_STYLE));
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        String contentTitle = notification.getTitle();
        String contentText= notification.getBody();

        NotificationCompat.Style notificationStyle=null;

        String summaryText = null;
        String bigText = null;
        String bigContentTitle= null;
        boolean autoCancel=true;
        if(dataMap!=null) {

            if(dataMap.get(NOTIFICATION_SUMMARY_TEXT)!=null)
                summaryText = dataMap.get(NOTIFICATION_SUMMARY_TEXT);

            if(dataMap.get(NOTIFICATION_BIG_TEXT)!=null)
                bigText = dataMap.get(NOTIFICATION_BIG_TEXT);

            if(dataMap.get(NOTIFICATION_BIG_CONTENT_TITLE)!=null)
                bigContentTitle = dataMap.get(NOTIFICATION_BIG_CONTENT_TITLE);

            if(dataMap.get(NOTIFICATION_AUTO_CANCEL)!=null)
                autoCancel  =dataMap.get(NOTIFICATION_AUTO_CANCEL).toString().equals("true");
        }
        if(style==STYLE_INBOX_STYLE)
        {
            notificationStyle= new NotificationCompat.InboxStyle(notificationBuilder)
                    .setBigContentTitle(bigContentTitle)
                    .setSummaryText(summaryText);
        }
        else if(style==STYLE_BIG_TEXT_STYLE)
        {
            notificationStyle= new NotificationCompat.BigTextStyle(notificationBuilder)
                    .bigText(bigText)
                    .setBigContentTitle(bigContentTitle)
                    .setSummaryText(summaryText);
        }
        else if(style==STYLE_BIG_PICTURE_STYLE)
        {
            Bitmap bitmap=null;
            if(dataMap!=null && dataMap.get(NOTIFICATION_IMAGE)!=null) {
                bitmap = getBitmapFromURL(dataMap.get(NOTIFICATION_IMAGE));
                /*
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(dataMap.get("image")).getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                notificationStyle= new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        .setBigContentTitle(bigContentTitle)
                        .setSummaryText(summaryText);
            }
        }


        if(notificationStyle!=null)
            notificationBuilder.setStyle(notificationStyle);


        notificationBuilder
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(autoCancel)
                .setLargeIcon(getActivityIcon(getPackageName()))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);;

        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
