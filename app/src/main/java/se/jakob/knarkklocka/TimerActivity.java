package se.jakob.knarkklocka;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import static se.jakob.knarkklocka.data.Alarm.STATE_ACTIVE;
import static se.jakob.knarkklocka.data.Alarm.STATE_DEAD;
import static se.jakob.knarkklocka.data.Alarm.STATE_SNOOZING;
import static se.jakob.knarkklocka.data.Alarm.STATE_WAITING;

public class TimerActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "TimerActivity";
    private TextView tv_due_time;
    private Chronometer chronometer;
    private ConstraintLayout timer_content_group;
    private ConstraintLayout chronometer_container;
    private FloatingActionButton fab_start_timer;

    private Button button_sleep_mode;
    private Button button_snooze;
    private MainActivityViewModel mainActivityViewModel;

    private Alarm currentAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timer_content_group = findViewById(R.id.timer_content);
        chronometer_container = findViewById(R.id.chronometer_container);
        chronometer = findViewById(R.id.time_display);
        tv_due_time = findViewById(R.id.tv_main_due);
        button_sleep_mode = findViewById(R.id.button_remove_timer);
        button_snooze = findViewById(R.id.button_snooze_timer);

        button_sleep_mode.setVisibility(View.INVISIBLE);
        tv_due_time.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);

        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mainActivityViewModel.getAlarm().observe(this, new Observer<Alarm>() {
            @Override
            public void onChanged(@Nullable final Alarm alarm) {
                if (alarm != null) {
                    currentAlarm = alarm;
                    int state = alarm.getState();
                    switch (state) {
                        case STATE_ACTIVE:
                            setupChronometer(alarm);
                            button_snooze.setVisibility(View.VISIBLE);
                            button_sleep_mode.setVisibility(View.VISIBLE);
                            fab_start_timer.setVisibility(View.VISIBLE);
                            fab_start_timer.setImageResource(R.drawable.ic_restart_black_24dp);
                            break;
                        case STATE_DEAD:
                            fab_start_timer.setVisibility(View.VISIBLE);
                            button_sleep_mode.setVisibility(View.VISIBLE);
                            break;
                        case STATE_SNOOZING:
                            setupChronometer(alarm);
                            button_snooze.setVisibility(View.INVISIBLE);
                            button_sleep_mode.setVisibility(View.VISIBLE);
                            fab_start_timer.setVisibility(View.VISIBLE);
                            fab_start_timer.setImageResource(R.drawable.ic_restart_black_24dp);
                            break;
                        case STATE_WAITING:
                            setupChronometer(alarm);
                            button_snooze.setVisibility(View.INVISIBLE);
                            button_sleep_mode.setVisibility(View.VISIBLE);
                            fab_start_timer.setVisibility(View.INVISIBLE);
                            fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp);
                            break;
                    }
                } else {
                    fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp);
                    fab_start_timer.setVisibility(View.VISIBLE);
                    button_sleep_mode.setVisibility(View.INVISIBLE);
                    button_snooze.setVisibility(View.INVISIBLE);
                    tv_due_time.setVisibility(View.INVISIBLE);
                    chronometer.setVisibility(View.INVISIBLE);
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
        fab_start_timer = findViewById(R.id.button_add_timer);


        fab_start_timer.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                callVibrate();
                restartAlarm();
                Snackbar.make(v, "Started new timer!", Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

        button_sleep_mode.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                sleep(v);
                return true;
            }
        });

        button_snooze.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });

    }
    private void callVibrate() {
        Klaxon.vibrateOnce(this);
    }

    private void setupChronometer(Alarm alarm) {
        DateFormat dateFormat = DateFormat.getTimeInstance();
        Date endTime = alarm.getEndTime();
        String dateString = dateFormat.format(endTime);
        tv_due_time.setText(dateString);
        tv_due_time.setVisibility(View.VISIBLE);
        chronometer.setVisibility(View.VISIBLE);
        long timeDelta = endTime.getTime() - System.currentTimeMillis();
        chronometer.setBase(SystemClock.elapsedRealtime() + timeDelta);
        chronometer.start();
    }

    public void setChronometer(Date base) {
        chronometer.setBase(base.getTime());
    }

    public void restartAlarm() {
        //TransitionManager.beginDelayedTransition(chronometer_container, new Slide(Gravity.TOP));
        if (alarmIsRunning()) {
            mainActivityViewModel.kill(); /* If there is an alarm running, kill it. */
            AlarmBroadcasts.broadcastStopAlarm(this); /* Stop any vibration or notifications that are happening right now */
        }
        TimerUtils.startMainTimer(this, mainActivityViewModel);
    }

    public void sleep(View v) {
        //TransitionManager.beginDelayedTransition(timer_content_group, new Slide(Gravity.TOP));
        fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp);
        if (alarmIsRunning()) {
            AlarmNotificationsBuilder.clearAllNotifications(this);
            Log.d(TAG, "Sleep mode engaged...");
            AlarmBroadcasts.broadcastStopAlarm(this); /* Stop any vibration or notifications that are happening right now */
            Snackbar.make(v, "Goodnight", Snackbar.LENGTH_LONG).show();
            Alarm currentAlarm = mainActivityViewModel.getAlarm().getValue();
            if (currentAlarm != null) {
                mainActivityViewModel.delete();
                TimerUtils.cancelAlarm(getApplicationContext(), currentAlarm.getId());
            }
        }
    }

    private void snooze() {
        AlarmBroadcasts.broadcastStopAlarm(this); /*Stop any vibration or notifications that are happening right now*/
        TimerUtils.startSnoozeTimer(this, mainActivityViewModel);
    }

    public boolean alarmIsRunning() {
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
        if (alarmIsRunning()) {
            chronometer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        chronometer.stop();
        Log.d(TAG, "Application paused chronometer stopped");
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
