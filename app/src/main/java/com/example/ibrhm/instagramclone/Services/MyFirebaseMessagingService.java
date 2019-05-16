package com.example.ibrhm.instagramclone.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.ibrhm.instagramclone.Home.ChatActivity;
import com.example.ibrhm.instagramclone.Home.DirectFragment;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "com.example.ibrhm.instagramclone.REQUEST_NOTIFICATION";
    private static final String CHANNEL_ID_ACCEPT = "com.example.ibrhm.instagramclone.ACCEPT_NOTIFICATION";
    private static final String CHANNEL_ID_MESSAGE = "com.example.ibrhm.instagramclone.MESSAGE_NOTIFICATION";
    private static final String CHANNEL_GROUP_MESSAGE = "com.example.ibrhm.instagramclone.MESSAGE_GROUP";
    private static final String CHANNEL_NAME = "Follow Request";
    private static final String CHANNEL_NAME_ACCEPT = "Follow Accept";
    private static final String CHANNEL_NAME_MESSAGE = "Message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String type = remoteMessage.getData().get("type");
        String user_name = remoteMessage.getData().get("user_name");
        String full_name = remoteMessage.getData().get("full_name");
        String user_image = remoteMessage.getData().get("user_image");
        String user_id = remoteMessage.getData().get("user_id");
        String click_action = remoteMessage.getData().get("click_action");

        if(type.equals("Follow_Request")){
            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id",user_id);
            resultIntent.putExtra("action", "request");

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            final NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                            .setSmallIcon(R.drawable.ic_notification_icon)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(full_name+" (@"+user_name+") has requested to follow you.")
                                    .setBigContentTitle("Instagram"))
                            .setColor(getResources().getColor(R.color.colorGreenPastel))
                            .setContentTitle("Instagram")
                            .setContentText(full_name+" (@"+user_name+") has requested to follow you.")
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setCategory(NotificationCompat.CATEGORY_SOCIAL);

            try {
                mBuilder.setLargeIcon(Picasso.get().load(user_image).placeholder(getDrawable(R.drawable.ic_default_avatar)).get());
            } catch (IOException e) {
                e.printStackTrace();
            }

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,NotificationManager.IMPORTANCE_LOW);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setLightColor(Color.WHITE);

                mBuilder.setChannelId(CHANNEL_ID);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(mChannel);
            }

            manager.notify(user_id,0, mBuilder.build());
        }
        else if(type.equals("Follow_Accept")){
            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id",user_id);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            final NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id_accept))
                            .setSmallIcon(R.drawable.ic_notification_icon)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(full_name+" (@"+user_name+") accepted your follow request.")
                                    .setBigContentTitle("Instagram"))
                            .setColor(getResources().getColor(R.color.colorGreenPastel))
                            .setContentTitle("Instagram")
                            .setContentText(full_name+" (@"+user_name+") accepted your follow request.")
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setCategory(NotificationCompat.CATEGORY_SOCIAL);

            try {
                mBuilder.setLargeIcon(Picasso.get().load(user_image).placeholder(Objects.requireNonNull(getDrawable(R.drawable.ic_default_avatar))).get());
            } catch (IOException e) {
                e.printStackTrace();
            }

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID_ACCEPT, CHANNEL_NAME_ACCEPT,NotificationManager.IMPORTANCE_LOW);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setLightColor(Color.WHITE);

                mBuilder.setChannelId(CHANNEL_ID_ACCEPT);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(mChannel);
            }

            manager.notify(user_id,1, mBuilder.build());
        }
        else if(type.equals("Message")){

            if(!ChatActivity.isOpenChat && !DirectFragment.isOpenDirect){
                String last_message = remoteMessage.getData().get("message");

                Intent resultIntent = new Intent(click_action);
                resultIntent.putExtra("user_id",user_id);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                final NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id_message))
                                .setSmallIcon(R.drawable.ic_notification_icon)
                                .setStyle(new NotificationCompat.InboxStyle()
                                        .addLine(user_name+": "+last_message)
                                        .setBigContentTitle(full_name))
                                .setColor(getResources().getColor(R.color.colorAccent))
                                .setContentTitle(full_name)
                                .setContentText(user_name+": "+last_message)
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true)
                                .setGroup(CHANNEL_GROUP_MESSAGE)
                                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setCategory(NotificationCompat.CATEGORY_SOCIAL);

                try {
                    mBuilder.setLargeIcon(Picasso.get().load(user_image).placeholder(Objects.requireNonNull(getDrawable(R.drawable.ic_default_avatar))).get());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID_MESSAGE, CHANNEL_NAME_MESSAGE,NotificationManager.IMPORTANCE_HIGH);
                    mChannel.enableLights(true);
                    mChannel.enableVibration(true);
                    mChannel.setLightColor(Color.WHITE);

                    mBuilder.setChannelId(CHANNEL_ID_MESSAGE);
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(mChannel);
                }

                manager.notify(user_id,2, mBuilder.build());
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        onCreateNewToken(token);
    }

    private void onCreateNewToken(String value) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("fcm_token").setValue(value);
        }
    }
}
