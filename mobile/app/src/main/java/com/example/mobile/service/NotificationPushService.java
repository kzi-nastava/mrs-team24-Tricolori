package com.example.mobile.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mobile.R;
import com.example.mobile.dto.notification.NotificationDto;
import com.example.mobile.network.service.StompNotificationService;
import com.example.mobile.ui.MainActivity;

/**
 * Foreground service that keeps the STOMP notification subscription alive
 * while the app is in the background.  Started/stopped by NotificationsFragment
 * on attach/detach and by MainActivity on login/logout.
 *
 * Usage:
 *   // Start (pass userId via Intent extra)
 *   Intent i = new Intent(context, NotificationPushService.class);
 *   i.putExtra("user_id", userId);
 *   ContextCompat.startForegroundService(context, i);
 *
 *   // Stop
 *   context.stopService(new Intent(context, NotificationPushService.class));
 */
public class NotificationPushService extends Service {

    private static final String TAG = "NotifPushService";

    // Notification channel IDs
    public static final String CHANNEL_SERVICE  = "notif_service_channel";   // foreground service
    public static final String CHANNEL_ALERTS   = "notif_alerts_channel";    // user-facing alerts
    public static final String CHANNEL_PANIC    = "notif_panic_channel";     // high-priority panic

    // Foreground service notification id
    private static final int FG_NOTIFICATION_ID = 1001;
    // Base id for user-facing push notifications (incremented per alert)
    private static int nextPushId = 2000;

    private StompNotificationService stompService;


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        stompService = new StompNotificationService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long userId = 0;
        if (intent != null) {
            userId = intent.getLongExtra("user_id", 0);
        }

        // If we didn't get a valid id from the intent, try SharedPrefs as fallback
        if (userId == 0) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userId = prefs.getLong("user_id", 0);
        }

        startForeground(FG_NOTIFICATION_ID, buildForegroundNotification());

        if (userId != 0) {
            final long finalUserId = userId;
            stompService.connect(finalUserId, this::onNotificationReceived);
        } else {
            Log.w(TAG, "No user_id — service will idle until restarted with valid user");
        }

        // If killed by system, restart and re-deliver last intent so we reconnect
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (stompService != null) stompService.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }


    private void onNotificationReceived(NotificationDto dto) {
        Log.d(TAG, "Push notification received: type=" + dto.getType());

        // Broadcast to any live NotificationsFragment
        Intent broadcast = new Intent("com.example.mobile.NEW_NOTIFICATION");
        broadcast.putExtra("notification_json", new com.google.gson.Gson().toJson(dto));
        sendBroadcast(broadcast);

        // Post a system-level push notification
        postPushNotification(dto);
    }


    private void postPushNotification(NotificationDto dto) {
        String title = getTitleForType(dto.getType());
        String body  = dto.getContent() != null ? dto.getContent() : "You have a new notification.";

        boolean isPanic = "RIDE_PANIC".equals(dto.getType());

        // Tap action — opens MainActivity (NavController will handle deep links if needed)
        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (dto.getRideId() != null) tapIntent.putExtra("ride_id", dto.getRideId());
        tapIntent.putExtra("notification_type", dto.getType());

        PendingIntent pi = PendingIntent.getActivity(
                this,
                (int) dto.getId(),
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channel = isPanic ? CHANNEL_PANIC : CHANNEL_ALERTS;
        int priority   = isPanic
                ? NotificationCompat.PRIORITY_MAX
                : NotificationCompat.PRIORITY_DEFAULT;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(priority)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (isPanic) {
            builder.setColor(0xFFD32F2F)        // red tint
                    .setColorized(true)
                    .setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(nextPushId++, builder.build());
    }


    private Notification buildForegroundNotification() {
        Intent tapIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, tapIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_SERVICE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Notifications active")
                .setContentText("Listening for ride updates and alerts")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setSilent(true)
                .setContentIntent(pi)
                .build();
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;

        // Silent foreground service channel
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                "Background Service",
                NotificationManager.IMPORTANCE_MIN
        );
        serviceChannel.setDescription("Keeps notification delivery active");
        serviceChannel.setShowBadge(false);
        nm.createNotificationChannel(serviceChannel);

        // Standard alerts
        NotificationChannel alertsChannel = new NotificationChannel(
                CHANNEL_ALERTS,
                "Ride & App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        alertsChannel.setDescription("Ride updates, ratings, and support messages");
        nm.createNotificationChannel(alertsChannel);

        // High-importance panic channel
        NotificationChannel panicChannel = new NotificationChannel(
                CHANNEL_PANIC,
                "Emergency Panic Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        panicChannel.setDescription("Emergency panic alerts requiring immediate attention");
        panicChannel.enableVibration(true);
        panicChannel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
        nm.createNotificationChannel(panicChannel);
    }

    private String getTitleForType(String type) {
        if (type == null) return "Notification";
        switch (type) {
            case "RIDE_PANIC":              return "🚨 Emergency Panic Alert";
            case "RIDE_STARTING":           return "Ride is starting";
            case "RIDE_STARTED":            return "Ride has started";
            case "RIDE_COMPLETED":          return "Ride completed";
            case "RIDE_CANCELLED":          return "Ride cancelled";
            case "RIDE_REJECTED":           return "Ride request rejected";
            case "ADDED_TO_RIDE":           return "Added to shared ride";
            case "RATING_REMINDER":         return "Rating reminder";
            case "RATING_RECEIVED":         return "You received a rating";
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER":  return "Upcoming ride reminder";
            case "RIDE_REPORT":             return "Ride issue reported";
            case "NEW_REGISTRATION":        return "New driver registered";
            case "PROFILE_CHANGE_REQUEST":  return "Profile change request";
            case "NEW_CHAT_MESSAGE":        return "New support message";
            default:                        return "Notification";
        }
    }
}