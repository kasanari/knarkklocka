package se.jakob.knarkklocka;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;

import java.util.Date;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.utils.TimerUtils;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID;

public class AlarmNotificationsBuilder {

    private static int ALARM_NOTIFICATION_ID = 1995;
    private static final String ALARM_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";
    private static final int ALARM_FIRING_NOTIFICATION_ID = 8;

    public static synchronized void showNotification(Service service, Alarm alarm) {

        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    ALARM_NOTIFICATION_CHANNEL_ID,
                    service.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setBypassDnd(true);
            mChannel.setShowBadge(false);
            mChannel.setLightColor(0xff0000);
            notificationManager.createNotificationChannel(mChannel);
        }


        Notification.Builder notification = new Notification.Builder(service, ALARM_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Alarm Title!")
                .setContentText("The is an alarm!")
                .setColor(ContextCompat.getColor(service, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_pill)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setLocalOnly(true);

        final Date base = alarm.getEndTime();

        // Setup Content Action
        //PendingIntent contentIntent = TimerUtils.getShowAlarmIntent(context, alarm.getId());
        //notification.setContentIntent(contentIntent);

        // Full screen intent has flags so it is different than the content intent.
        final Intent fullScreen = new Intent(service, AlarmActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                .putExtra(EXTRA_ALARM_ID, alarm.getId());

        final PendingIntent pendingFullScreen = PendingIntent.getActivity(service, 0, fullScreen, FLAG_UPDATE_CURRENT);
        notification.setFullScreenIntent(pendingFullScreen, true);

        notification.setContentIntent(pendingFullScreen);

        service.startForeground(ALARM_FIRING_NOTIFICATION_ID, notification.build());
    }

}
