package se.jakob.knarkklocka

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_timer.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.settings.SettingsActivity
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.Klaxon
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.utils.Utils
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel

class TimerActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private var currentAlarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        Utils.checkIfWhiteListed(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)

        button_remove_timer.visibility = View.INVISIBLE
        chronometer_main.visibility = View.INVISIBLE

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        viewModel.liveAlarm.observe(this, Observer { alarm ->
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
                chronometer_main.visibility = View.INVISIBLE
            }
        })

        /* Setting up Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Timer setup"
        setSupportActionBar(toolbar)

        /* Setting up OnClick listeners */
        fab_start_timer.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(this)
            restartAlarm()
            Snackbar.make(v, "Started new timer!", Snackbar.LENGTH_LONG).show()
            true
        }

        button_remove_timer.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(this)
            sleep(v)
            true
        }

        button_snooze_timer.setOnClickListener { v -> snooze(v) }

    }

    private fun setupChronometer(alarm: Alarm) {
        val endTime = alarm.endTime
        val timeDelta = endTime.time - System.currentTimeMillis()
        chronometer_main.base = SystemClock.elapsedRealtime() + timeDelta
        chronometer_main.start()
        chronometer_main.visibility = View.VISIBLE
    }

    private fun restartAlarm() {
        viewModel.kill() /* Kill any running alarms. */
        TimerUtils.startMainTimer(this)
    }

    private fun sleep(v: View) {
        viewModel.sleep() /* Delete or kill any running alarm */
        currentAlarm?.let {
            TimerUtils.cancelAlarm(this, it.id)
            Log.d(TAG, "Sleep mode engaged...")
            Snackbar.make(v, "Goodnight", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun snooze(v: View) {
        currentAlarm?.let {alarm ->
            TimerUtils.startSnoozeTimer(this, alarm)
            Snackbar.make(v, "You are only postponing the inevitable...", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timer, menu) // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.hasAlarm) {
            chronometer_main.start()
        }
    }

    override fun onPause() {
        super.onPause()
        chronometer_main.stop()
        Log.d(TAG, "Application paused, chronometer stopped")
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
