package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.Date;

public class AlarmActivityViewModel extends AndroidViewModel implements AlarmViewModel {
    private LiveData<Alarm> mAlarm;
    private AlarmRepository mRepository;

    public AlarmActivityViewModel(Application application) {
        super(application);
        mRepository = new AlarmRepository(application);
        this.mAlarm = mRepository.getActiveAlarm();
    }

    public LiveData<Alarm> getAlarm() {
        return mAlarm;
    }

    public Alarm getCurrentAlarm() {
        return mAlarm.getValue();
    }

    public long add(Alarm alarm) {
        return mRepository.insert(alarm);
    }

    public void delete() {
        if (mAlarm.getValue() != null) {
            mRepository.delete(mAlarm.getValue());
        }
    }

    public void kill() {
        if (isAlarmRunning()) {
            Alarm alarm = mAlarm.getValue();
            alarm.setState(Alarm.STATE_DEAD);
            mRepository.update(alarm);
        }
    }

    public void snooze(Date endTime) {
        if (mAlarm.getValue() != null) {
            Alarm alarm = mAlarm.getValue();
            alarm.setState(Alarm.STATE_SNOOZING);
            alarm.incrementSnoozes();
            alarm.setEndTime(endTime);
            mRepository.update(alarm);
        }
    }

    public boolean isAlarmRunning() {
        return mAlarm.getValue() != null;
    }
}
