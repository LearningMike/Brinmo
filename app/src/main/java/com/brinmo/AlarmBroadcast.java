package com.brinmo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class AlarmBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String text = bundle.getString("bid");
        String alarmMessage = bundle.getString("amess");
        String bizname = bundle.getString("bizname");
        String username = bundle.getString("username");

        //Click on Notification

        Intent intent1 = new Intent(context, RatingActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("bid", text);
        Intent intentc = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/2349033872404"));

        //Notification Builder
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent chatIntent = PendingIntent.getActivity(context, 1, intentc, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "brinmo_critical");

        //Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        mBuilder.setSmallIcon(R.drawable.ic_brinmo_logo);
        mBuilder.setContentTitle("Has "+bizname+ " delivered?");
        mBuilder.setContentText(alarmMessage);
        //mBuilder.setLargeIcon(largeIcon);
        mBuilder.addAction(R.drawable.ic_menu_review, "Yes", pendingIntent);
        mBuilder.addAction(R.drawable.ic_menu_report, "No", chatIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setCategory(NotificationCompat.CATEGORY_ALARM);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setColor(Color.parseColor("#000011"));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(alarmMessage)
                .setBigContentTitle("Has "+bizname+" delivered?")
                .setSummaryText(username));
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.build().flags = NotificationCompat.PRIORITY_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "brinmo_critical";
            NotificationChannel channel = new NotificationChannel(channelId, "Brinmo Critical Updates", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);


    }
}
