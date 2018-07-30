package se.jakob.knarkklocka.data;

import android.app.Application;

public class AlarmActivityViewModel extends AlarmViewModel {
    public AlarmActivityViewModel(Application application) {
        super(application);
    }

    public void setAlarm(long id) {
        super.mAlarm = mRepository.getLiveAlarmByID(id);
    }
}
