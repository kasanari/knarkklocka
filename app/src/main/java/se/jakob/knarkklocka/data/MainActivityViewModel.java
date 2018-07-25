package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.Date;

public class MainActivityViewModel extends AlarmViewModel {
    public MainActivityViewModel(Application application) {
        super(application);
        super.mAlarm = mRepository.getWaitingAlarm();
    }
}


