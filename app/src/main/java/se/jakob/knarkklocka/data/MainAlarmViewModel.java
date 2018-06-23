package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

public class MainAlarmViewModel extends AndroidViewModel {
    private LiveData<Alarm> mAlarm;
    private AlarmRepository mRepository;

    public MainAlarmViewModel(Application application) {
        super(application);
        mRepository = new AlarmRepository(application);
        this.mAlarm = mRepository.getAlarmsByState(Alarm.STATE_WAITING);
    }

    public LiveData<Alarm> getAlarm() {
        return mAlarm;
    }

    public void insert(Alarm alarm) {
        mRepository.insert(alarm);
    }

    public void delete() {
        mRepository.delete(mAlarm.getValue());
    }
}
