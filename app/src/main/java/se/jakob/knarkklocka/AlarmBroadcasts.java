package se.jakob.knarkklocka;

import android.content.Context;
import android.content.Intent;

import static se.jakob.knarkklocka.utils.TimerUtils.ACTION_STOP_ALARM;

public class AlarmBroadcasts {

    public static void broadcastStopAlarm(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_STOP_ALARM);
        context.sendBroadcast(intent);
    }
}
