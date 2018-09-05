package se.jakob.knarkklocka

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.content_timer.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.settings.SettingsActivity
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.Klaxon
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel
import java.text.DateFormat
import java.util.*

class TimerActivity : AppCompatActivity() {

    private lateinit var mainActivityViewModel: MainActivityViewModel

    private var currentAlarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        button_remove_timer.visibility = View.INVISIBLE
        tv_main_due.visibility = View.INVISIBLE
        chronometer_main.visibility = View.INVISIBLE

        /*Floating action button to start a new timer*/

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)
        mainActivityViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        mainActivityViewModel.liveAlarm.observe(this, Observer { alarm ->
            if (alarm != null) {
                currentAlarm = alarm
                val state = alarm.state
                when (state) {
                    AlarmState.STATE_ACTIVE -> {
                        setupChronometer(alarm)
                        button_snooze_timer.visibility = View.VISIBLE
                        button_remove_timer.visibility = View.VISIBLE
                        fab_start_timer.visibility = View.VISIBLE
                        fab_start_timer.setImageResource(R.drawable.ic_restart_black_24dp)
                    }
                    AlarmState.STATE_DEAD -> {
                        fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp)
                        fab_start_timer.visibility = View.VISIBLE
                        button_remove_timer.visibility = View.INVISIBLE
                        button_snooze_timer.visibility = View.INVISIBLE
                        tv_main_due.visibility = View.INVISIBLE
                        chronometer_main.visibility = View.INVISIBLE
                    }
                    AlarmState.STATE_SNOOZING -> {
                        setupChronometer(alarm)
                        button_snooze_timer.visibility = View.INVISIBLE
                        button_remove_timer.visibility = View.VISIBLE
                        fab_start_timer.visibility = View.VISIBLE
                        fab_start_timer.setImageResource(R.drawable.ic_restart_black_24dp)
                    }
                    AlarmState.STATE_WAITING -> {
                        setupChronometer(alarm)
                        button_snooze_timer.visibility = View.INVISIBLE
                        button_remove_timer.visibility = View.VISIBLE
                        fab_start_timer.visibility = View.INVISIBLE
                        fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp)
                    }
                }
            } else {
                fab_start_timer.setImageResource(R.drawable.ic_alarm_blue_24dp)
                fab_start_timer.visibility = View.VISIBLE
                button_remove_timer.visibility = View.INVISIBLE
                button_snooze_timer.visibility = View.INVISIBLE
                tv_main_due.visibility = View.INVISIBLE
                chronometer_main.visibility = View.INVISIBLE
            }
        })

        /* Setting up Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Timer setup"
        setSupportActionBar(toolbar)

        fab_start_timer.setOnLongClickListener { v ->
            callVibrate()
            restartAlarm()
            Snackbar.make(v, "Started new timer!", Snackbar.LENGTH_LONG).show()
            true
        }

        button_remove_timer.setOnLongClickListener { v ->
            sleep(v)
            true
        }

        button_snooze_timer.setOnClickListener { snooze() }

    }

    private fun callVibrate() {
        Klaxon.vibrateOnce(this)
    }

    private fun setupChronometer(alarm: Alarm) {
        val dateFormat = DateFormat.getTimeInstance()
        val endTime = alarm.endTime
        val dateString = dateFormat.format(endTime)
        tv_main_due.text = dateString
        tv_main_due.visibility = View.VISIBLE
        chronometer_main.visibility = View.VISIBLE
        val timeDelta = endTime.time - System.currentTimeMillis()
        chronometer_main.base = SystemClock.elapsedRealtime() + timeDelta
        chronometer_main.start()
    }

    private fun restartAlarm() {
        if (mainActivityViewModel.hasAlarm) {
            mainActivityViewModel.kill() /* If there is an alarm running, kill it. */
            AlarmBroadcasts.broadcastStopAlarm(this) /* Stop any vibration or notifications that are happening right now */
        }
        TimerUtils.startMainTimer(this, mainActivityViewModel)
    }

    private fun sleep(v: View) {
        if (mainActivityViewModel.hasAlarm) {
            AlarmNotificationsUtils.clearAllNotifications(this)
            AlarmBroadcasts.broadcastStopAlarm(this) /* Stop any vibration or notifications that are happening right now */
            mainActivityViewModel.delete()
            currentAlarm?.let {
                TimerUtils.cancelAlarm(applicationContext, it.id)
            Log.d(TAG, "Sleep mode engaged...")
            Snackbar.make(v, "Goodnight", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun snooze() {
        AlarmBroadcasts.broadcastStopAlarm(this) /*Stop any vibration or notifications that are happening right now*/
        TimerUtils.startSnoozeTimer(this, mainActivityViewModel)
    }

    private fun alarmIsRunning(): Boolean {
        return mainActivityViewModel.liveAlarm.value != null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_timer, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        if (mainActivityViewModel.hasAlarm) {
            chronometer_main.start()
        }
    }

    override fun onPause() {
        super.onPause()
        chronometer_main.stop()
        Log.d(TAG, "Application paused chronometer stopped")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return when (id) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            R.id.action_history -> {
                val historyIntent = Intent(this, HistoryActivity::class.java)
                startActivity(historyIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "TimerActivity"
    }
}
