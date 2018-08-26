package se.jakob.knarkklocka;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import java.util.Date;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.utils.TimerUtils;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID;

public class AlarmNotificationsUtils {

    public static final int ALARM_ACTIVE_NOTIFICATION_ID = 1235;
    private static final String ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID = "firing_notification_channel";
    private static final String ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID = "snooze_notification_channel";
    private static final String ALARM_WAITING_NOTIFICATION_CHANNEL_ID = "waiting_notification_channel";
    private static final int ALARM_WAITING_NOTIFICATION_ID = 5464;
    private static final int ALARM_SNOOZING_NOTIFICATION_ID = 9845;

    private static final int LIGHT_COLOR_RED = 0xff0000;
    private static final int LIGHT_COLOR_BLUE = 0x0000ff;

    private static void setupActiveNotificationChannel(Service service) {
        NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID,
                        service.getString(R.string.active_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
                mChannel.enableVibration(true);
                mChannel.enableLights(true);
                mChannel.setBypassDnd(true);
                mChannel.setShowBadge(false);
                mChannel.setLightColor(LIGHT_COLOR_RED);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    private static void setupSnoozeNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.snoozing_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW);
                mChannel.setBypassDnd(true);
                mChannel.setShowBadge(false);
                mChannel.enableLights(true);
                mChannel.setLightColor(LIGHT_COLOR_BLUE);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    private static void setupWaitingNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        ALARM_WAITING_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.waiting_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW);
                mChannel.setBypassDnd(true);
                mChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    public static synchronized void showActiveAlarmNotification(Service service, Alarm alarm) {

        setupActiveNotificationChannel(service);

        Notification.Builder notification = new Notification.Builder(service, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(true)
                .setLocalOnly(true)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID);


        final String pname = service.getPackageName();
        String stateText = "Time for drugs!";
        notification.setCustomContentView(buildChronometer(pname, alarm.getEndTime(), true, stateText));

        // Full screen intent has flags so it is different than the content intent.
        final Intent fullScreen = new Intent(service, AlarmActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                .putExtra(EXTRA_ALARM_ID, alarm.getId());

        final PendingIntent pendingFullScreen = PendingIntent.getActivity(service, 0, fullScreen, FLAG_UPDATE_CURRENT);
        notification.setFullScreenIntent(pendingFullScreen, true);

        // Setup Content Intent
        notification.setContentIntent(pendingFullScreen);

        service.startForeground(ALARM_ACTIVE_NOTIFICATION_ID, notification.build());
    }

    public static synchronized void showSnoozingAlarmNotification(Context context, Alarm alarm) {

        setupSnoozeNotificationChannel(context);

        Notification.Builder notification = new Notification.Builder(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(false)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setLocalOnly(true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID);

        final PendingIntent pendingShowAlarm = TimerUtils.INSTANCE.getTimerActivityIntent(context, alarm.getId());
        notification.setContentIntent(pendingShowAlarm);

        final String pname = context.getPackageName();
        String stateText = "The drug timer is snoozing.";
        notification.setCustomContentView(buildChronometer(pname, alarm.getEndTime(), true, stateText));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ALARM_SNOOZING_NOTIFICATION_ID, notification.build());
    }

    public static synchronized void showWaitingAlarmNotification(Context context, Alarm alarm) {

        setupWaitingNotificationChannel(context);

        Notification.Builder notification = new Notification.Builder(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_WAITING_NOTIFICATION_CHANNEL_ID);

        final PendingIntent pendingShowAlarm = TimerUtils.INSTANCE.getTimerActivityIntent(context, alarm.getId());
        notification.setContentIntent(pendingShowAlarm);

        final String pname = context.getPackageName();

        String stateText = "The drug timer is running.";
        notification.setCustomContentView(buildChronometer(pname, alarm.getEndTime(), true, stateText));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ALARM_WAITING_NOTIFICATION_ID, notification.build());
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    private static RemoteViews buildChronometer(String pname, Date endtime, boolean running,
                                                CharSequence stateText) {
        final RemoteViews content = new RemoteViews(pname, R.layout.chronometer_notif_content);
        content.setChronometerCountDown(R.id.notif_chronometer, true);

        long timeDelta = endtime.getTime() - System.currentTimeMillis();
        long base = SystemClock.elapsedRealtime() + timeDelta;
        content.setChronometer(R.id.notif_chronometer, base, null, running);
        content.setTextViewText(R.id.notif_state, stateText);
        return content;
    }

}
