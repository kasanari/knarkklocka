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
        getVibrator(context).run {
            vibrate(vibe)
        }
    }

    fun vibrateAlarm(context: Context) {
        val effect = VibrationEffect.createWaveform(mVibratePattern, amps, 0)
        getVibrator(context).run {
            vibrate(effect)
            Log.d(TAG, "Vibration started")
        }
    }

    fun stopVibrate(context: Context) {
        getVibrator(context).run {
            cancel()
            Log.d(TAG, "Vibration stopped")
        }
    }

    private fun getVibrator(context: Context) : Vibrator {
        return context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}
