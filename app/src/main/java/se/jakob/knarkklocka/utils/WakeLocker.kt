package se.jakob.knarkklocka.utils

import android.content.Context
import android.os.PowerManager
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.Log
import se.jakob.knarkklocka.BuildConfig

/**
 * Functions for making it more convenient to create and cancel wakelocks.
 */
object WakeLocker {
    private const val TAG = ":knarkklocka:WakeLocker"
    private lateinit var wakeLock: PowerManager.WakeLock
    private var locked = false

    /**
     * Acquire a wakelock, turning on the screen and keeping the device awake.
     */
    fun acquire(context: Context) {
        if (locked) {
            wakeLock.release()
        }
        wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, TAG).apply {
                acquire(10 * MINUTE_IN_MILLIS)
                locked = true
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Acquired WakeLock.")
                }
            }
        }
    }

    /**
     * Release the acquired wakelock.
     */
    fun release() {
        if (locked) {
            try {
                wakeLock.release()
                locked = false
            } catch (e: Exception) { // If there is an exception here, the wakelock was probably already released.
                Log.e(TAG, e.message)
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Released WakeLock.")
            }
        }
    }
}
