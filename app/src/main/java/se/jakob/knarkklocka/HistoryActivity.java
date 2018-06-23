package se.jakob.knarkklocka;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmHistoryViewModel;
import se.jakob.knarkklocka.data.AlarmListAdapter;

public class HistoryActivity extends AppCompatActivity {
    private AlarmHistoryViewModel mAlarmHistoryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_recyclerview);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final AlarmListAdapter adapter = new AlarmListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mAlarmHistoryViewModel = ViewModelProviders.of(this).get(AlarmHistoryViewModel.class);

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground
        mAlarmHistoryViewModel.getAllAlarms().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(@Nullable final List<Alarm> alarms) {
                // Update the cached copy of the words in the adapter.
                adapter.setAlarms(alarms);
            }
        });
    }
}
