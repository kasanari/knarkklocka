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
        if (action != null){TimerUtils.executeTask(this, action);}
    }
}
