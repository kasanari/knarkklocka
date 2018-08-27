package se.jakob.knarkklocka.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

object Klaxon {
    private const val TAG = "Klaxon"
    fun vibrateOnce(context: Context) {
        val vibe = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(vibe)
    }

    fun vibrateAlarm(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        val mVibratePattern = longArrayOf(1000, 1500)
        val amps = intArrayOf(255, 0)
        val effect = VibrationEffect.createWaveform(mVibratePattern, amps, 0)
        if (!vibrator.hasAmplitudeControl()) {
            Log.d(TAG, "Device does not have amplitude control")
        }
        vibrator?.vibrate(effect)
    }

    fun stopVibrate(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.cancel()
    }

}
