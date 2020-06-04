package com.rcoe.allindia;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Uri sound;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this,Constants.CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo))
                .setSmallIcon(R.drawable.logo)
                .setColor(Color.WHITE)
                .setContentTitle(title)
                .setContentText(body)
                .setVibrate(new long[]{500,1000})
                .setAutoCancel(true)
                .setSound(sound);

        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        int id = (int) System.currentTimeMillis();

        mNotificationManager.notify(id,builder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(Constants.CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{500,1000});
            mNotificationManager.createNotificationChannel(channel);
        }
    }
}
