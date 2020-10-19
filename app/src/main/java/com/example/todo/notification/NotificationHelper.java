package com.example.todo.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.todo.MainActivity;
import com.example.todo.R;

public class NotificationHelper extends ContextWrapper {

    public static final String channel1ID = "channel1ID",
    channel1Name = "TaskNotification", GROUP_KEY_TASK_REMINDER = "com.android.todo.notification.TASK_REMINDER";

    private int color;

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.color.app_color, typedValue, true);
        color = typedValue.data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel channel1 = new NotificationChannel(channel1ID, channel1Name, NotificationManager.IMPORTANCE_HIGH);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(color);
        channel1.enableVibration(true);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel1);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    public Notification getChannelNotification(String title, String message, String listID, int notificationID) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, intent, 0);

        Log.d("NotificationHelper", "NotificationHelper constructor called." + " " + listID);

        Intent actionIntent = new Intent(this, FinishReceiver.class);
        actionIntent.putExtra("listID", listID);
        actionIntent.putExtra("taskID", notificationID);
        PendingIntent pendingActionIntent = PendingIntent.getBroadcast(this, 1000+notificationID, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        Notification summaryNotification = new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setStyle(new NotificationCompat.InboxStyle()
                .addLine("Title2: Message2")
                .addLine("Title1: Message1")
                .setBigContentTitle("2 task reminders")
                .setSummaryText("ilyasfarjad@gmail.com"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(GROUP_KEY_TASK_REMINDER)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                .build();*/

        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color))
                .addAction(R.drawable.ic_notif, "Mark as completed", pendingActionIntent)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_TASK_REMINDER)
                .build();
    }

}
