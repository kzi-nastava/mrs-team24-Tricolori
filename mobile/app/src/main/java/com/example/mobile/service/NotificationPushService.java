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

public class NotificationPushService extends Service
        implements StompNotificationService.NotificationListener {

    private static final String TAG = "NotifPushService";

    public static final String EXTRA_USER_EMAIL = "user_email";
    public static final String EXTRA_JWT_TOKEN  = "jwt_token";

    public static final String ACTION_NEW_NOTIFICATION = "com.example.mobile.NEW_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_JSON = "notification_json";

    public static final String CHANNEL_SERVICE = "notif_service_channel";
    public static final String CHANNEL_ALERTS  = "notif_alerts_channel";
    public static final String CHANNEL_PANIC   = "notif_panic_channel";

    private static final int FG_NOTIFICATION_ID = 1001;
    private static int nextPushId = 2000;

    private StompNotificationService stompService;
    private final com.google.gson.Gson gson = new com.google.gson.Gson();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        stompService = new StompNotificationService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String email = null;
        String jwt   = null;

        if (intent != null) {
            email = intent.getStringExtra(EXTRA_USER_EMAIL);
            jwt   = intent.getStringExtra(EXTRA_JWT_TOKEN);
        }

        if (email != null && jwt != null) {
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("user_email", email)
                    .putString("jwt_token",  jwt)
                    .apply();
        } else {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            email = prefs.getString("user_email", null);
            jwt   = prefs.getString("jwt_token",  null);
        }

        startForeground(FG_NOTIFICATION_ID, buildForegroundNotification());

        if (email != null && jwt != null) {
            if (stompService.isConnected()) stompService.disconnect();
            stompService.connect(email, jwt, this);
        } else {
            Log.w(TAG, "Missing credentials — service will idle until restarted");
        }

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
        return null;
    }

    @Override
    public void onNotificationReceived(NotificationDto dto) {
        Log.d(TAG, "Notification received: " + dto.getType());

        Intent broadcast = new Intent(ACTION_NEW_NOTIFICATION);
        broadcast.putExtra(EXTRA_NOTIFICATION_JSON, gson.toJson(dto));

        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(broadcast);

        postPushNotification(dto);
    }

    @Override
    public void onConnectionStateChanged(boolean connected) {
        Log.d(TAG, "STOMP " + (connected ? "connected" : "disconnected"));
    }

    private void postPushNotification(NotificationDto dto) {
        boolean isPanic = "RIDE_PANIC".equals(dto.getType());
        String  title   = getTitleForType(dto.getType());
        String  body    = dto.getContent() != null ? dto.getContent() : "You have a new notification.";

        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (dto.getRideId() != null) tapIntent.putExtra("ride_id", dto.getRideId());
        tapIntent.putExtra("notification_type", dto.getType());

        PendingIntent pi = PendingIntent.getActivity(
                this,
                nextPushId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, isPanic ? CHANNEL_PANIC : CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(isPanic
                        ? NotificationCompat.PRIORITY_MAX
                        : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (isPanic) {
            builder.setColor(0xFFD32F2F)
                    .setColorized(true)
                    .setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(nextPushId++, builder.build());
    }

    private Notification buildForegroundNotification() {
        PendingIntent pi = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE);

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

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE, "Background Service", NotificationManager.IMPORTANCE_MIN);
        serviceChannel.setDescription("Keeps notification delivery active");
        serviceChannel.setShowBadge(false);
        nm.createNotificationChannel(serviceChannel);

        NotificationChannel alertsChannel = new NotificationChannel(
                CHANNEL_ALERTS, "Ride & App Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        alertsChannel.setDescription("Ride updates, ratings, and support messages");
        nm.createNotificationChannel(alertsChannel);

        NotificationChannel panicChannel = new NotificationChannel(
                CHANNEL_PANIC, "Emergency Panic Alerts", NotificationManager.IMPORTANCE_HIGH);
        panicChannel.setDescription("Emergency panic alerts requiring immediate attention");
        panicChannel.enableVibration(true);
        panicChannel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
        nm.createNotificationChannel(panicChannel);
    }

    private String getTitleForType(String type) {
        if (type == null) return "Notification";
        switch (type) {
            case "RIDE_PANIC":             return "🚨 Emergency Panic Alert";
            case "RIDE_STARTING":          return "Ride is starting";
            case "RIDE_STARTED":           return "Ride has started";
            case "RIDE_COMPLETED":         return "Ride completed";
            case "RIDE_CANCELLED":         return "Ride cancelled";
            case "RIDE_REJECTED":          return "Ride request rejected";
            case "ADDED_TO_RIDE":          return "Added to shared ride";
            case "RATING_REMINDER":        return "Rating reminder";
            case "RATING_RECEIVED":        return "You received a rating";
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER": return "Upcoming ride reminder";
            case "RIDE_REPORT":            return "Ride issue reported";
            case "NEW_REGISTRATION":       return "New driver registered";
            case "PROFILE_CHANGE_REQUEST": return "Profile change request";
            case "NEW_CHAT_MESSAGE":       return "New support message";
            default:                       return "Notification";
        }
    }
}