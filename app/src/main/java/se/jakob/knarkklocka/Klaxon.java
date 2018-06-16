package se.jakob.knarkklocka;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class Klaxon {
    public static void vibrateOnce(Context context) {
        VibrationEffect vibe = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(vibe);
        }
    }

    public static void vibrateAlarm(Context context) {
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        long[] mVibratePattern = new long[]{0, 500, 600, 700, 800};
        VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, 1);
        if (vibrator != null) {
            if (!vibrator.hasAmplitudeControl()) {
                Log.d("DEBUG", "Device does not have amplitude control");
            }
            vibrator.vibrate(effect);
        }
    }

    public static void stopVibrate(Context context) {
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        if (vibrator != null) vibrator.cancel();
    }

}
