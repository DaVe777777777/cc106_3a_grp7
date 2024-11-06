package com.example.project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "PetNotifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("notificationTitle");
        String message = intent.getStringExtra("notificationText");

        // Log to verify intent extras
        Log.d("NotificationReceiver", "Title: " + title + ", Message: " + message);
        sendNotification(context, title, message);
    }

    private void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, "Pet Notifications", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, HomeFragment.class); // Ensure this points to your main activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("FRAGMENT_TO_OPEN", "NotificationFragment");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        int notificationId = (int) System.currentTimeMillis(); // Change this strategy for unique IDs if necessary

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d("NotificationReceiver", "Notification sent with ID: " + notificationId);
        } else {
            Log.e("NotificationReceiver", "Notification Manager is null!");
        }
    }

}