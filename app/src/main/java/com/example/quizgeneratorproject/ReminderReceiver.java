package com.example.quizgeneratorproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "study_reminders";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        int day = intent.getIntExtra("day", -1);

        // 1) On Android 13+, ensure POST_NOTIFICATIONS was granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission denied; skipping notify()");
            return;
        }

        // 2) Build your notification
        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time to study")
                .setContentText("Your scheduled study session is starting now.")
                .setAutoCancel(true);

        // 3) Safely post it
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        try {
            nm.notify(200 + day, nb.build());
        } catch (SecurityException se) {
            // In case the permission was revoked at runtime
            Log.e(TAG, "Failed to post notification, permission missing", se);
        }
    }
}
