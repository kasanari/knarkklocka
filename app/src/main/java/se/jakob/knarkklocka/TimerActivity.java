package se.jakob.knarkklocka;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import se.jakob.knarkklocka.data.Alarm;
import se.jakob.knarkklocka.data.MainActivityViewModel;
import se.jakob.knarkklocka.settings.SettingsActivity;
import se.jakob.knarkklocka.utils.TimerUtils;

public class TimerActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "TimerActivity";
    TextView due_time_view;
    Chronometer countdown_view;
    ConstraintLayout timer_content_group;
    ConstraintLayout chronometer_container;
    FloatingActionButton start_timer_fab;

    Button dismiss_timer_button;
    Button snooze_timer_button;
    private MainActivityViewModel mainActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        timer_content_group = findViewById(R.id.timer_content);
        chronometer_container = findViewById(R.id.chronometer_container);
        countdown_view = findViewById(R.id.time_display);
        //chronometer_container.setVisibility(View.GONE);

        snooze_timer_button = findViewById(R.id.button_snooze_timer);
        dismiss_timer_button = findViewById(R.id.button_remove_timer);
        //snooze_timer_button.setVisibility(View.INVISIBLE);
        //dismiss_timer_button.setVisibility(View.INVISIBLE);

        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        due_time_view = findViewById(R.id.tv_main_due);

        mainActivityViewModel.getAlarm().observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    DateFormat dateFormat = DateFormat.getTimeInstance();
                    Date endTime = alarm.getEndTime();
                    String dateString = dateFormat.format(endTime);
                    due_time_view.setText(dateString);
                    due_time_view.setVisibility(View.VISIBLE);
                    countdown_view.setVisibility(View.VISIBLE);
                    long timeDelta = endTime.getTime() - System.currentTimeMillis();
                    countdown_view.setBase(SystemClock.elapsedRealtime() + timeDelta);
                    countdown_view.start();
                } else {
                    due_time_view.setVisibility(View.INVISIBLE);
                    countdown_view.setVisibility(View.INVISIBLE);
                }
            }
        });

        /*Setup the shared preference listener*/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        /*Use toolbar instead of ActionBar*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Timer setup");
        setSupportActionBar(toolbar);


        /*Floating action button to start a new timer*/
        start_timer_fab = findViewById(R.id.button_add_timer);
        start_timer_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //callVibrate();
                setAlarm();
                Snackbar.make(view, "Set new timer!", Snackbar.LENGTH_LONG).show();
            }

        });

        start_timer_fab.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showTimePickerDialog(v);
                return true;
            }
        });

    }

    public void callVibrate() {
        //TimerUtils.vibrateOnce(this);
    }

    public void setChronometer(Date base) {
        countdown_view.setBase(base.getTime());
    }

    public void setAlarm() {
        //TransitionManager.beginDelayedTransition(chronometer_container, new Slide(Gravity.TOP));
        //countdown_view.setVisibility(View.VISIBLE);
        //chronometer_container.setVisibility(View.VISIBLE);
        //TransitionManager.beginDelayedTransition(timer_content_group, new Slide(Gravity.BOTTOM));
        //snooze_timer_button.setVisibility(View.VISIBLE);
        //dismiss_timer_button.setVisibility(View.VISIBLE);
        if (isAlarmRunning()) {
            mainActivityViewModel.delete();
        }
        long timer_duration = PreferenceUtils.getMainTimerLength(this);
        Calendar currentTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MILLISECOND, (int)timer_duration);
        final Alarm alarm = new Alarm(Alarm.STATE_WAITING, currentTime.getTime(), endTime.getTime());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                long id = mainActivityViewModel.insert(alarm);
                TimerUtils.setNewAlarm(getApplicationContext(), id, alarm.getEndTime());
            }
        });

    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void sleep(View v) {
        //TransitionManager.beginDelayedTransition(timer_content_group, new Slide(Gravity.TOP));
        //snooze_timer_button.setVisibility(View.INVISIBLE);
        //dismiss_timer_button.setVisibility(View.INVISIBLE);
        //countdown_view.setVisibility(View.INVISIBLE);
        //chronometer_container.setVisibility(View.GONE);
        Log.d(TAG, "Sleep mode engaged...");
        if (isAlarmRunning()) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Alarm currentAlarm = mainActivityViewModel.getAlarm().getValue();
                    if (currentAlarm != null) {
                        mainActivityViewModel.delete();
                        TimerUtils.cancelAlarm(getApplicationContext(), currentAlarm.getId());
                    }
                }
            });
        }
    }

    public boolean isAlarmRunning() {
        return mainActivityViewModel.getAlarm().getValue() != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAlarmRunning()) {
            countdown_view.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        countdown_view.stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdown_view.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        countdown_view.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_history:
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivity(historyIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
