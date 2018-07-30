package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.Date;

public class AlarmActivityViewModel extends AlarmViewModel {
    public AlarmActivityViewModel(Application application) {
        super(application);
    }

    public void setAlarm(long id) {
        super.mAlarm = mRepository.getLiveAlarmByID(id);
    }
}
