package se.jakob.knarkklocka

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews

import java.util.Date

import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.utils.TimerUtils

import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID

object AlarmNotificationsUtils {

    val ALARM_ACTIVE_NOTIFICATION_ID = 1235
    private val ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID = "firing_notification_channel"
    private val ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID = "snooze_notification_channel"
    private val ALARM_WAITING_NOTIFICATION_CHANNEL_ID = "waiting_notification_channel"
    private val ALARM_WAITING_NOTIFICATION_ID = 5464
    private val ALARM_SNOOZING_NOTIFICATION_ID = 9845

    private val LIGHT_COLOR_RED = 0xff0000
    private val LIGHT_COLOR_BLUE = 0x0000ff

    private fun setupActiveNotificationChannel(service: Service) {
        val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                        ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID,
                        service.getString(R.string.active_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH)
                mChannel.enableVibration(true)
                mChannel.enableLights(true)
                mChannel.setBypassDnd(true)
                mChannel.setShowBadge(false)
                mChannel.lightColor = LIGHT_COLOR_RED
                notificationManager.createNotificationChannel(mChannel)
            }
        }
    }

    private fun setupSnoozeNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                        ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.snoozing_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW)
                mChannel.setBypassDnd(true)
                mChannel.setShowBadge(false)
                mChannel.enableLights(true)
                mChannel.lightColor = LIGHT_COLOR_BLUE
                notificationManager.createNotificationChannel(mChannel)
            }
        }
    }

    private fun setupWaitingNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                        ALARM_WAITING_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.waiting_notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW)
                mChannel.setBypassDnd(true)
                mChannel.setShowBadge(false)
                notificationManager.createNotificationChannel(mChannel)
            }
        }
    }

    @Synchronized
    fun showActiveAlarmNotification(service: Service, alarm: Alarm) {

        setupActiveNotificationChannel(service)

        val notification = Notification.Builder(service, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(service.resources, R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(true)
                .setLocalOnly(true)
                .setStyle(Notification.DecoratedCustomViewStyle())
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)


        val pname = service.packageName
        val stateText = "Time for drugs!"
        notification.setCustomContentView(buildChronometer(pname, alarm.endTime, true, stateText))

        // Full screen intent has flags so it is different than the content intent.
        val fullScreen = Intent(service, AlarmActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                .putExtra(EXTRA_ALARM_ID, alarm.id)

        val pendingFullScreen = PendingIntent.getActivity(service, 0, fullScreen, FLAG_UPDATE_CURRENT)
        notification.setFullScreenIntent(pendingFullScreen, true)

        // Setup Content Intent
        notification.setContentIntent(pendingFullScreen)

        service.startForeground(ALARM_ACTIVE_NOTIFICATION_ID, notification.build())
    }

    @Synchronized
    fun showSnoozingAlarmNotification(context: Context, alarm: Alarm) {

        setupSnoozeNotificationChannel(context)

        val notification = Notification.Builder(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(false)
                .setStyle(Notification.DecoratedCustomViewStyle())
                .setLocalOnly(true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID)

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context, alarm.id)
        notification.setContentIntent(pendingShowAlarm)

        val pname = context.packageName
        val stateText = "The drug timer is snoozing."
        notification.setCustomContentView(buildChronometer(pname, alarm.endTime, true, stateText))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALARM_SNOOZING_NOTIFICATION_ID, notification.build())
    }

    @Synchronized
    fun showWaitingAlarmNotification(context: Context, alarm: Alarm) {

        setupWaitingNotificationChannel(context)

        val notification = Notification.Builder(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.pill))
                .setOngoing(true)
                .setAutoCancel(false)
                .setLocalOnly(true)
                .setStyle(Notification.DecoratedCustomViewStyle())
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(ALARM_WAITING_NOTIFICATION_CHANNEL_ID)

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context, alarm.id)
        notification.setContentIntent(pendingShowAlarm)

        val pname = context.packageName

        val stateText = "The drug timer is running."
        notification.setCustomContentView(buildChronometer(pname, alarm.endTime, true, stateText))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALARM_WAITING_NOTIFICATION_ID, notification.build())
    }

    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    private fun buildChronometer(pname: String, endtime: Date, running: Boolean,
                                 stateText: CharSequence): RemoteViews {
        val content = RemoteViews(pname, R.layout.chronometer_notif_content)
        content.setChronometerCountDown(R.id.notif_chronometer, true)

        val timeDelta = endtime.time - System.currentTimeMillis()
        val base = SystemClock.elapsedRealtime() + timeDelta
        content.setChronometer(R.id.notif_chronometer, base, null, running)
        content.setTextViewText(R.id.notif_state, stateText)
        return content
    }

}
