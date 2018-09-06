package se.jakob.knarkklocka.utils

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.RemoteViews
import se.jakob.knarkklocka.AlarmActivity
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import java.text.SimpleDateFormat
import java.util.*

object AlarmNotificationsUtils {

    private const val ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID = "firing_notification_channel"
    private const val ALARM_SNOOZE_NOTIFICATION_CHANNEL_ID = "snooze_notification_channel"
    private const val ALARM_WAITING_NOTIFICATION_CHANNEL_ID = "waiting_notification_channel"

    private const val ALARM_WAITING_NOTIFICATION_ID = 5464
    private const val ALARM_SNOOZING_NOTIFICATION_ID = 9845
    private const val ALARM_ACTIVE_NOTIFICATION_ID = 1235

    private const val LIGHT_COLOR_RED = 0xff0000
    private const val LIGHT_COLOR_BLUE = 0x0000ff

    private fun setupActiveNotificationChannel(service: Service) {
        val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    private fun setupSnoozeNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    private fun setupWaitingNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        val packageName = service.packageName
        val stateText = service.resources.getString(R.string.active_notification_text)
        notification.setCustomContentView(buildActiveNotificationView(packageName, alarm, true, stateText))

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

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)
        notification.setContentIntent(pendingShowAlarm)

        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.snoozing_notification_text)
        notification.setCustomContentView(buildSnoozingNotificationView(context, packageName, alarm, true, stateText))

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

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)
        notification.setContentIntent(pendingShowAlarm)

        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.waiting_notification_text)

        notification.setCustomContentView(buildWaitingNotificationView(packageName, alarm.endTime, true, stateText))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALARM_WAITING_NOTIFICATION_ID, notification.build())
    }

    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    private fun buildChronometer(packageName: String, endTime: Date, running: Boolean): RemoteViews {
        val content = RemoteViews(packageName, R.layout.chronometer_notif_content)
        content.setChronometerCountDown(R.id.notif_chronometer, true)
        val timeDelta = endTime.time - System.currentTimeMillis()
        val base = SystemClock.elapsedRealtime() + timeDelta
        content.setChronometer(R.id.notif_chronometer, base, null, running)

        return content
    }

    private fun buildWaitingNotificationView(packageName: String, endTime: Date, running: Boolean,
                                             stateText: CharSequence): RemoteViews {
        val content = buildChronometer(packageName, endTime, running)
        content.setTextViewText(R.id.notif_state, stateText)
        val dateString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(endTime)
        val detailString = String.format(Locale.getDefault(), "This timer is due %s", dateString)
        content.setTextViewText(R.id.notif_details, detailString)
        return content
    }

    private fun buildSnoozingNotificationView(context: Context, packageName: String, alarm: Alarm,
                                              running: Boolean, stateText: CharSequence): RemoteViews {

        val content = buildChronometer(packageName, alarm.endTime, running)
        content.setTextViewText(R.id.notif_state, stateText)
        val snoozesString = context.resources.getQuantityString(R.plurals.times, alarm.snoozes, alarm.snoozes)
        val snoozeText = String.format(Locale.getDefault(), "This timer has been snoozed %s.", snoozesString)
        content.setTextViewText(R.id.notif_details, snoozeText)
        return content
    }

    private fun buildActiveNotificationView(packageName: String, alarm: Alarm,
                                            running: Boolean, stateText: CharSequence): RemoteViews {
        val content = buildChronometer(packageName, alarm.endTime, running)
        content.setTextViewText(R.id.notif_state, stateText)
        content.setViewVisibility(R.id.notif_details, View.GONE)
        return content
    }
}
