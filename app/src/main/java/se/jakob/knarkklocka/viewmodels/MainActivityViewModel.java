package se.jakob.knarkklocka.viewmodels;

import android.app.Application;

public class MainActivityViewModel extends AlarmViewModel {
    public MainActivityViewModel(Application application) {
        super(application);
        super.mAlarm = mRepository.getCurrentAlarm();
    }
}


