package com.example.todo.notification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.todo.notification.NotificationHelper;

public class AlertReceiver extends BroadcastReceiver {

    public static String title = "Task Reminder", message = "You scheduled a notification for this task";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlertReceiver", "AlertReceiver constructor called." + " " + intent.getStringExtra("listID"));
        NotificationHelper notificationHelper = new NotificationHelper(context);
        Notification nb = notificationHelper.getChannelNotification(title, intent.getStringExtra("message"), intent.getStringExtra("listID"),
                intent.getIntExtra("notificationID", 0));
        notificationHelper.getManager().notify(intent.getIntExtra("notificationID", 0), nb);
    }
}
