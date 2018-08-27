package se.jakob.knarkklocka.utils

import android.content.Context
import android.os.PowerManager
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.Log
import se.jakob.knarkklocka.BuildConfig

object WakeLocker {
    private const val TAG = "WakeLocker"
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = wakeLock ?: pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, TAG).also {
            it.acquire(10 * MINUTE_IN_MILLIS)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Acquired WakeLock")
            }
        }
    }

    fun release() {
        wakeLock?.run {
            this.release()
            wakeLock = null
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Released WakeLock")
            }
        }
    }
}
