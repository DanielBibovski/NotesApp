package com.example.notesapp.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.notesapp.R;

public class NotificationUtil {

    private NotificationUtil() {
    }

    private static final String CHANNEL_ID = "notes_reminder_channel_id";
    private static final String CHANNEL_NAME = "Note Reminder";

    public static void sendNotification(Context context, int id, String noteText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = getNotification(context, id, noteText);
        notificationManager.notify(id, notification);

    }

    private static Notification getNotification(Context context, int id, String noteText) {
        createChannel(context);

        Bundle args = new Bundle();
        args.putInt("noteId", id);
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation)
                .setDestination(R.id.noteDetailsFragmentDestination)
                .setArguments(args)
                .createPendingIntent();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setContentTitle("Notes Reminder");
        builder.setContentText(noteText);
        builder.setSmallIcon(R.drawable.ic_notes);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        return builder.build();
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
