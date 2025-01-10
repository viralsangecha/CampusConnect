package com.example.campusconnect;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleDailyNotification(Context context) {
        // Calculate the initial delay
        Calendar currentTime = Calendar.getInstance();
        Calendar next8AM = Calendar.getInstance();
        next8AM.set(Calendar.HOUR_OF_DAY, 8);
        next8AM.set(Calendar.MINUTE, 0);
        next8AM.set(Calendar.SECOND, 0);

        if (currentTime.after(next8AM)) {
            // If it's already past 8 AM today, schedule for tomorrow
            next8AM.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = next8AM.getTimeInMillis() - currentTime.getTimeInMillis();

        // Create the WorkRequest
        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                AttendanceNotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        // Enqueue the WorkRequest
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyAttendanceNotification",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest);
    }
}
