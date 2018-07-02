package se.jakob.knarkklocka;

import android.app.IntentService;
import android.content.Intent;

import se.jakob.knarkklocka.utils.TimerUtils;

public class TimerIntentService extends IntentService {

    public TimerIntentService() {
        super("TimerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra(TimerUtils.EXTRA_ALARM_ID, -1);
        if (action != null) {
            TimerUtils.executeTask(this, action, id);
        }
    }
}
