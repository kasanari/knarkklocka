package se.jakob.knarkklocka.utils

import android.app.*
import android.app.PendingIntent.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import se.jakob.knarkklocka.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.utils.TimerUtils.ALARM_INTENT_ID
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import se.jakob.knarkklocka.utils.TimerUtils.getAlarmActionIntent
import se.jakob.knarkklocka.utils.TimerUtils.getAlarmServiceIntent
import java.text.SimpleDateFormat
import java.util.*

/**
 * Functions for creating and removing notifications used by the app.
 */
object AlarmNotificationsUtils {

    private const val ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID = "firing_notification_channel"
    private const val ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID = "snooze_notification_channel"
    private const val ALARM_WAITING_NOTIFICATION_CHANNEL_ID = "waiting_notification_channel"
    private const val ALARM_MISSED_NOTIFICATION_CHANNEL_ID = "missed_notification_channel"

    private const val ALARM_WAITING_NOTIFICATION_ID = 5464
    private const val ALARM_SNOOZING_NOTIFICATION_ID = 9845
    private const val ALARM_ACTIVE_NOTIFICATION_ID = 1235
    private const val ALARM_MISSED_NOTIFICATION_ID = 4231

    private const val LIGHT_COLOR_RED = 0xff0000
    private const val LIGHT_COLOR_BLUE = 0x0000ff

    private const val TAG = "AlarmNotificationsUtils"

    /**
     * Create all notification channels used by the app.
     */
    fun setupChannels(context: Context) {
        setupNotificationChannel(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_MISSED_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_WAITING_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID)
    }

    /**
     * Remove all notification channels used by the app.
     */
    fun removeAllChannels(context: Context) {
        removeChannel(context, ALARM_MISSED_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_WAITING_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
    }

    /**
     * Remove a notification channel.
     * @param id The ID of the channel to be removed.
     */
    private fun removeChannel(context: Context, id: String) {
        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            deleteNotificationChannel(id)
        }
    }

    /**
     * Generate a base notification, with settings common to all styles of notifications.
     * @param channel_id The ID of the channel this notification belongs to.
     * @return A [Notification.Builder] with common settings already applied.
     */
    private fun baseNotification(context: Context, channel_id: String): Notification.Builder {
        return Notification.Builder(context, channel_id).apply {
            setOngoing(true) // Notifications should not be dismissible.
            setAutoCancel(false) // Notifications should not be dismissed when touched.
            setLocalOnly(true) // Notifications should not bridge to other devices.
            style = Notification.DecoratedCustomViewStyle()
            setVisibility(Notification.VISIBILITY_PUBLIC) // Notification is fully visible on lock screen.
            setSmallIcon(R.drawable.ic_alarm_white_24dp)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.pill))
            setColor(ContextCompat.getColor(context, R.color.colorAccent))
        }
    }

    /**
     * Set up notification channels used by the app.
     * @param channel_id The ID of the channel to be created.
     */
    private fun setupNotificationChannel(context: Context, channel_id: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = when (channel_id) {
                ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID -> getSnoozeNotificationChannel(context)
                ALARM_MISSED_NOTIFICATION_CHANNEL_ID -> getMissedNotificationChannel(context)
                ALARM_WAITING_NOTIFICATION_CHANNEL_ID -> getWaitingNotificationChannel(context)
                ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID -> getActiveNotificationChannel(context)
                else -> {
                    null
                }
            }
            channel?.setSound(null, null) // Notification should be silent.
            notificationManager.createNotificationChannel(channel!!)
        }
    }

    /**
     * Get the notification channel used for notifications when an alarm is firing.
     * @return The [NotificationChannel] used to indicate an [Alarm] has reached its end time.
     */
    private fun getActiveNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.active_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
            enableLights(true) // Notification should make the notification light turn on.
            setBypassDnd(true) // Notifications should bypass Do Not Disturb mode.
            setShowBadge(false) // Notifications should not show application badges.
            lightColor = LIGHT_COLOR_RED
        }
    }

    /**
     * Get the notification channel used for notifications when an alarm has been snoozed.
     * @return The [NotificationChannel] used to indicate an [Alarm] has reached its end time.
     */
    private fun getSnoozeNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.snoozing_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW).apply {
            setBypassDnd(true) // Notifications should bypass Do Not Disturb mode.
            setShowBadge(false) // Notifications should not show application badges.
            enableLights(false) // Notifications wont make the notification light turn on.
            lightColor = LIGHT_COLOR_BLUE
        }
    }

    /**
     * Get the notification channel used for notifications when an alarm has been missed.
     * @return The [NotificationChannel] used to indicate an [Alarm] has been missed.
     */
    private fun getMissedNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_MISSED_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.snoozing_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
            setBypassDnd(true) // Notifications should bypass Do Not Disturb mode.
            setShowBadge(false) // Notifications should not show application badges.
            enableLights(true) // Notification should make the notification light turn on.
            lightColor = LIGHT_COLOR_RED
        }
    }

    /**
     * Get the notification channel used for notifications while an alarm is running in the background.
     * @return The [NotificationChannel] used to indicate an [Alarm] is waiting for its end time.
     */
    private fun getWaitingNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_WAITING_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.waiting_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW).apply {
            setBypassDnd(true) // Notifications should bypass Do Not Disturb mode.
            setShowBadge(false) // Notifications should not show application badges.
            enableLights(false) // Notification wont make the notification light turn on.
        }
    }

    @Synchronized
    fun showMissedAlarmNotification(service : Service, alarm: Alarm) {
        clearAllNotifications(service.applicationContext)
        val notification = getMissedAlarmNotification(service, alarm)
        service.startForeground(ALARM_MISSED_NOTIFICATION_ID, notification)
        Log.d(TAG, "Showing MISSED notification.")
    }

    private fun getMissedAlarmNotification(context: Context, alarm: Alarm) : Notification {
        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)
        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.missed_notification_text)

        val dismissAction = getRestartAction(context.applicationContext, alarm)
        val snoozeAction = getSnoozeAction(context.applicationContext, alarm)

        return baseNotification(context, ALARM_MISSED_NOTIFICATION_CHANNEL_ID).run {
            setCategory(Notification.CATEGORY_ALARM)
            setContentIntent(pendingShowAlarm)
            setCustomContentView(buildMissedNotificationView(packageName, alarm, true, stateText))
            addAction(dismissAction)
            addAction(snoozeAction)
            build()
        }
    }

    private fun getActiveAlarmNotification(context: Context, alarm: Alarm) : Notification {
        // Full screen intent has flags so it is different than the content intent.
        val fullScreen = Intent(context, AlarmActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }

        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.active_notification_text)
        val pendingFullScreen = PendingIntent.getActivity(context, 0, fullScreen, FLAG_UPDATE_CURRENT)

        val dismissAction = getRestartAction(context.applicationContext, alarm)
        val snoozeAction = getSnoozeAction(context.applicationContext, alarm)

        return baseNotification(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID).run {
            setCategory(Notification.CATEGORY_ALARM)
            setCustomContentView(buildActiveNotificationView(packageName, alarm, true, stateText))
            setFullScreenIntent(pendingFullScreen, true)
            setContentIntent(pendingFullScreen)
            addAction(dismissAction)
            addAction(snoozeAction)
            build()
        }
    }

    @Synchronized
    fun showActiveAlarmNotification(service: Service, alarm: Alarm) {
        clearAllNotifications(service.applicationContext)

        val notification = getActiveAlarmNotification(service, alarm)
        service.startForeground(ALARM_ACTIVE_NOTIFICATION_ID, notification)
        Log.d(TAG, "Showing ACTIVE notification.")
    }

    @Synchronized
    fun showSnoozingAlarmNotification(context: Context, alarm: Alarm) {
        clearAllNotifications(context)

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)
        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.snoozing_notification_text)

        val sleepAction = getSleepAction(context, alarm)
        val startAction = getRestartAction(context, alarm)

        val notification = baseNotification(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID).apply {
            setCategory(Notification.CATEGORY_ALARM)
            setCustomContentView(buildSnoozingNotificationView(context, packageName, alarm, true, stateText))
            setContentIntent(pendingShowAlarm)
            addAction(sleepAction)
            addAction(startAction)
        }

        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            notify(ALARM_SNOOZING_NOTIFICATION_ID, notification.build())
        }
        Log.d(TAG, "Showing SNOOZING notification.")
    }

    @Synchronized
    fun showWaitingAlarmNotification(context: Context, alarm: Alarm) {
        clearAllNotifications(context)

        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.waiting_notification_text)
        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)

        val action = getSleepAction(context, alarm)

        val notification = baseNotification(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID).apply {
            setCategory(Notification.CATEGORY_SERVICE)
            setContentIntent(pendingShowAlarm)
            setCustomContentView(buildWaitingNotificationView(packageName, alarm.endTime, true, stateText))
            addAction(action)
        }

        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            notify(ALARM_WAITING_NOTIFICATION_ID, notification.build())
        }
        Log.d(TAG, "Showing WAITING notification.")
    }


    private fun getSleepAction(context: Context, alarm: Alarm) : Notification.Action {

        val intent = getAlarmActionIntent(context, ACTION_SLEEP, alarm)

        val pendingIntent: PendingIntent =
                getBroadcast(context, ALARM_INTENT_ID, intent, FLAG_UPDATE_CURRENT)

        return  Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_remove_black),
                context.getString(R.string.remove),
                pendingIntent).build()
    }

    private fun getRestartAction(context: Context, alarm: Alarm) : Notification.Action {

        val intent = getAlarmActionIntent(context, ACTION_RESTART, alarm)

        val pendingIntent: PendingIntent =
                getBroadcast(context, ALARM_INTENT_ID, intent, FLAG_UPDATE_CURRENT)

        return Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_remove_black),
                context.getString(R.string.restart),
                pendingIntent).build()
    }

    private fun getSnoozeAction(context: Context, alarm: Alarm) : Notification.Action {

        val intent = getAlarmActionIntent(context, ACTION_SNOOZE, alarm)

        val pendingIntent: PendingIntent =
                getBroadcast(context, ALARM_INTENT_ID, intent, FLAG_UPDATE_CURRENT)

        return Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_remove_black),
                context.getString(R.string.snooze),
                pendingIntent).build()
    }

    @Synchronized
    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        Log.d(TAG, "Notifications cleared.")
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

    private fun buildMissedNotificationView(packageName: String, alarm: Alarm,
                                            running: Boolean, stateText: CharSequence): RemoteViews {
        val content = buildChronometer(packageName, alarm.endTime, running)
        content.setTextViewText(R.id.notif_state, stateText)
        content.setViewVisibility(R.id.notif_details, View.GONE)
        return content
    }
}
