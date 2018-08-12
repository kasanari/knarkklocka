package se.jakob.knarkklocka;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.AlarmHistoryViewModel;
import se.jakob.knarkklocka.data.AlarmListAdapter;
import se.jakob.knarkklocka.settings.SettingsActivity;
import se.jakob.knarkklocka.utils.TimerUtils;

public class HistoryActivity extends AppCompatActivity {

    AlarmHistoryViewModel mAlarmHistoryViewModel;

    Alarm currentAlarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
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

        mAlarmHistoryViewModel.getAlarm().observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable Alarm alarm) {
                currentAlarm = alarm;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clear_history:
                if (alarmIsRunning()) {
                    TimerUtils.cancelAlarm(getApplicationContext(), currentAlarm.getId());
                    AlarmNotificationsBuilder.clearAllNotifications(this);
                }

                mAlarmHistoryViewModel.clearHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public boolean alarmIsRunning() {
        return mAlarmHistoryViewModel.getAlarm().getValue() != null;
    }
}
