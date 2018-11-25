package se.jakob.knarkklocka.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

object Klaxon {
    private const val TAG = "Klaxon"
    private val mVibratePattern = longArrayOf(1000, 1500)
    private  val amps = intArrayOf(255, 0)

    fun vibrateOnce(context: Context) {
        val vibe = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        context.getSystemService(Vibrator::class.java).run {
            vibrate(vibe)
        }
    }

    fun vibrateAlarm(context: Context) {
        val effect = VibrationEffect.createWaveform(mVibratePattern, amps, 0)
        context.getSystemService(Vibrator::class.java).run {
            vibrate(effect)
            Log.d(TAG, "Vibration started")
        }
    }

    fun stopVibrate(context: Context) {
        context.getSystemService(Vibrator::class.java)?.run {
            cancel()
            Log.d(TAG, "Vibration stopped")
        }
    }
}
