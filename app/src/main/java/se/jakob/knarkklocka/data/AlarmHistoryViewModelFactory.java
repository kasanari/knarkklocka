package se.jakob.knarkklocka.data;

import android.arch.lifecycle.ViewModelProvider;

public class AlarmHistoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AlarmDatabase mDb;
    private final int mTaskId;


    public AlarmHistoryViewModelFactory(AlarmDatabase database, int taskId) {
        mDb = database;
        mTaskId = taskId;
    }
/*
    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AlarmHistoryViewModel(mDb, mTaskId);
    }*/
}
