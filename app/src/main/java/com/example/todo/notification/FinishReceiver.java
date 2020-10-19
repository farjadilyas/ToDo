package com.example.todo.notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class FinishReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, final Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(intent.getIntExtra("taskID",0));

        final Context finalContext = context;

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent deleteTaskIntent = new Intent(context, AlertReceiver.class);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        ref.child("isSynced").setValue(false);
        DatabaseReference listRef = ref.child("list").child(Objects.requireNonNull(intent.getStringExtra("listID")));

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(finalContext, intent.getIntExtra("taskID",0), deleteTaskIntent, 0);
                alarmManager.cancel(pendingIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listRef.removeValue();
    }
}
