package se.jakob.knarkklocka.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {
    private AlarmRepository mRepository;
    private LiveData<List<Alarm>> mAllAlarms;

    public AlarmViewModel(Application application) {
        super(application);
        mRepository = new AlarmRepository(application);
        mAllAlarms = mRepository.getAllAlarms();
    }

    LiveData<List<Alarm>> getmAllAlarms() {
        return mAllAlarms;
    }

    public void insert(Alarm alarm) {
        mRepository.insert(alarm);
    }


}
