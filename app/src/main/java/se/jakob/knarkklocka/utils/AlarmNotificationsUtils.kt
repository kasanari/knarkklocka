package se.jakob.knarkklocka.utils

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes.USAGE_NOTIFICATION
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import se.jakob.knarkklocka.AlarmActivity
import se.jakob.knarkklocka.BuildConfig
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.utils.TimerUtils.EXTRA_ALARM_ID
import java.text.SimpleDateFormat
import java.util.*

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

    private val mVibratePattern = longArrayOf(1000, 1500, 1000)
    private const val TAG = "AlarmNotificationsUtils"

    fun setupChannels(context: Context) {
        setupNotificationChannel(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_MISSED_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_WAITING_NOTIFICATION_CHANNEL_ID)
        setupNotificationChannel(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID)
    }

    fun removeAllChannels(context: Context) {
        removeChannel(context, ALARM_MISSED_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_WAITING_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID)
        removeChannel(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID)
    }

    private fun removeChannel(context: Context, id: String) {
        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            deleteNotificationChannel(id)
        }
    }

    private fun baseNotification(context: Context, channel_id: String): Notification.Builder {
        return Notification.Builder(context, channel_id).apply {
            setOngoing(true)
            setAutoCancel(false)
            setLocalOnly(true)
            style = Notification.DecoratedCustomViewStyle()
            setVisibility(Notification.VISIBILITY_PUBLIC)
            setSmallIcon(R.drawable.ic_alarm_white_24dp)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.pill))
            setColor(ContextCompat.getColor(context, R.color.colorAccent))
        }
    }

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
            channel?.setSound(null, null)
            notificationManager.createNotificationChannel(channel!!)
        }
    }

    private fun getActiveNotificationChannel(context: Context): NotificationChannel {
            return NotificationChannel(
                    ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.active_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                setBypassDnd(true)
                setShowBadge(false)
                lightColor = LIGHT_COLOR_RED
            }
    }

    private fun getSnoozeNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.snoozing_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW).apply {
            setBypassDnd(true)
            setShowBadge(false)
            enableLights(true)
            lightColor = LIGHT_COLOR_BLUE
        }
    }

    private fun getMissedNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_MISSED_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.snoozing_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
            setBypassDnd(true)
            setShowBadge(false)
            enableLights(true)
            lightColor = LIGHT_COLOR_RED
        }
    }

    private fun getWaitingNotificationChannel(context: Context): NotificationChannel {
        return NotificationChannel(
                ALARM_WAITING_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.waiting_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW).apply {
            setBypassDnd(true)
            setShowBadge(false)
        }
    }

    @Synchronized
    fun showMissedAlarmNotification(service: Service, alarm: Alarm) {
        clearAllNotifications(service)

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(service)
        val packageName = service.packageName
        val stateText = service.resources.getString(R.string.missed_notification_text)

        baseNotification(service, ALARM_MISSED_NOTIFICATION_CHANNEL_ID).run {
            setCategory(Notification.CATEGORY_ALARM)
            setContentIntent(pendingShowAlarm)
            setCustomContentView(buildMissedNotificationView(packageName, alarm, true, stateText))
            service.startForeground(ALARM_MISSED_NOTIFICATION_ID, build())
        }
        Log.d(TAG, "Showing MISSED notification.")
    }

    @Synchronized
    fun showActiveAlarmNotification(service: Service, alarm: Alarm) {
        clearAllNotifications(service)

        // Full screen intent has flags so it is different than the content intent.
        val fullScreen = Intent(service, AlarmActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }

        val packageName = service.packageName
        val stateText = service.resources.getString(R.string.active_notification_text)
        val pendingFullScreen = PendingIntent.getActivity(service, 0, fullScreen, FLAG_UPDATE_CURRENT)

        baseNotification(service, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID).run {
            setCategory(Notification.CATEGORY_ALARM)
            setCustomContentView(buildActiveNotificationView(packageName, alarm, true, stateText))
            setFullScreenIntent(pendingFullScreen, true)
            setContentIntent(pendingFullScreen)
            service.startForeground(ALARM_ACTIVE_NOTIFICATION_ID, build())
        }
        Log.d(TAG, "Showing ACTIVE notification.")
    }

    @Synchronized
    fun showSnoozingAlarmNotification(context: Context, alarm: Alarm) {
        clearAllNotifications(context)

        val pendingShowAlarm = TimerUtils.getTimerActivityIntent(context)
        val packageName = context.packageName
        val stateText = context.resources.getString(R.string.snoozing_notification_text)

        val notification = baseNotification(context, ALARM_SNOOZING_NOTIFICATION_CHANNEL_ID).apply {
            setCategory(Notification.CATEGORY_ALARM)
            setCustomContentView(buildSnoozingNotificationView(context, packageName, alarm, true, stateText))
            setContentIntent(pendingShowAlarm)
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
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
        val notification = baseNotification(context, ALARM_ACTIVE_NOTIFICATION_CHANNEL_ID).apply {
            setCategory(Notification.CATEGORY_SERVICE)
            setContentIntent(pendingShowAlarm)
            setCustomContentView(buildWaitingNotificationView(packageName, alarm.endTime, true, stateText))
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
            notify(ALARM_WAITING_NOTIFICATION_ID, notification.build())
        }
        Log.d(TAG, "Showing WAITING notification.")
    }

    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
