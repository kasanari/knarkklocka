package se.jakob.knarkklocka.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmRepository;

public class AlarmHistoryViewModel extends AlarmViewModel {
    private LiveData<List<Alarm>> mAllAlarms;

    public AlarmHistoryViewModel(Application application) {
        super(application);
        mRepository = new AlarmRepository(application);
        super.mAlarm = mRepository.getCurrentAlarm();
        mAllAlarms = mRepository.getAllAlarms();
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return mAllAlarms;
    }

    public void clearHistory() {
        mRepository.deleteAll();
    }

}
