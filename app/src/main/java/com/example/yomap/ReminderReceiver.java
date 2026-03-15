package com.example.yomap;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String username = UserSession.getUsername();

        // Get all teams where the user is a manager
        db.collection("Teams")
                .whereArrayContains("managers", username)
                .get()
                .addOnSuccessListener(teamSnapshot -> {
                    if (teamSnapshot.isEmpty()) return;

                    for (DocumentSnapshot teamDoc : teamSnapshot.getDocuments()) {
                        String teamId = teamDoc.getId();
                        String teamName = teamDoc.getString("title");

                        // Count unresolved reports in this team
                        db.collection("Teams")
                                .document(teamId)
                                .collection("Reports")
                                .whereNotEqualTo("status", 2) // unresolved
                                .get()
                                .addOnSuccessListener(reportSnapshot -> {
                                    int unresolvedCount = reportSnapshot.size();
                                    if (unresolvedCount > 0) {
                                        showNotification(context, teamName, unresolvedCount);
                                    }
                                });
                    }
                });
    }

    private void showNotification(Context context, String teamName, int count) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "daily_reminder";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Daily Report Reminder", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily reminder for unresolved reports");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // use your app icon
                .setContentTitle("Unresolved Reports in " + teamName)
                .setContentText("You have " + count + " unresolved reports")
                .setAutoCancel(true);

        manager.notify(teamName.hashCode(), builder.build());
    }
}