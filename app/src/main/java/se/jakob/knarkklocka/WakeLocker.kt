package se.jakob.knarkklocka

import android.content.Context
import android.os.PowerManager
import android.util.Log

object WakeLocker {
    private const val TAG = "WakeLocker"
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = wakeLock ?: pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, TAG)
        wakeLock?.acquire(3000)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Acquired WakeLock")
        }
    }

    fun release() {
        wakeLock?.let {
            it.release()
            wakeLock = null
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Released WakeLock")
            }
        }
    }
}
