package se.jakob.knarkklocka.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class AlarmHistoryViewModel extends ViewModel {
    private AlarmRepository mRepository;
    private LiveData<List<Alarm>> mAllAlarms;

    public AlarmHistoryViewModel(AlarmRepository repository) {
        mRepository = repository;
        mAllAlarms = mRepository.getAllAlarms();
    }

    LiveData<List<Alarm>> getmAllAlarms() {
        return mAllAlarms;
    }

    public void insert(Alarm alarm) {
        mRepository.insert(alarm);
    }


}
