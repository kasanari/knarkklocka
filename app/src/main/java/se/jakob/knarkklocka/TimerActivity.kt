package se.jakob.knarkklocka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.timer_main.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState.*
import se.jakob.knarkklocka.settings.SettingsActivity
import se.jakob.knarkklocka.ui.ChronometerFragment
import se.jakob.knarkklocka.ui.ControllerFragment
import se.jakob.knarkklocka.ui.CustomTimerSettingsFragment
import se.jakob.knarkklocka.utils.*
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils.clearAllNotifications
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils.showSnoozingAlarmNotification
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils.showWaitingAlarmNotification
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel

class TimerActivity : AppCompatActivity(), ControllerFragment.OnControllerEventListener {

    private lateinit var viewModel: MainActivityViewModel

    private var currentAlarm: Alarm? = null

    private var chronometerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timer_main)

        AlarmNotificationsUtils.setupChannels(this)

        Utils.checkIfWhiteListed(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        viewModel.liveAlarm.observe(this, Observer { alarm ->
            if (alarm != null) {
                currentAlarm = alarm
                val state = alarm.state
                when (state) {
                    STATE_ACTIVE -> {
                        displayChronometer()
                        hideSettings()
                    }
                    STATE_DEAD -> {
                        hideChronometer()
                        displaySettings()
                    }
                    STATE_SNOOZING -> {
                        hideSettings()
                        displayChronometer()
                        showSnoozingAlarmNotification(this, alarm)
                    }
                    STATE_WAITING -> {
                        hideSettings()
                        displayChronometer()
                        showWaitingAlarmNotification(this, alarm)
                    }
                    STATE_MISSED -> {
                        hideSettings()
                        displayChronometer()
                    }
                }
            } else {
                displaySettings()
                hideChronometer()
                clearAllNotifications(this)
            }
        })

        /* Setting up Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Timer control"
        setSupportActionBar(toolbar)

    }

    override fun onControllerEvent(v: View, event: String) {
        when (event) {
            ACTION_RESTART_ALARM -> {
                restartAlarm()
                showSnackBar(v, R.string.snackbar_alarm_created)
            }
            ACTION_SNOOZE_ALARM -> {
                snooze()
                showSnackBar(v, R.string.snackbar_alarm_snoozed)
            }
            ACTION_SLEEP -> {
                sleep()
                showSnackBar(v, R.string.snackbar_alarm_cancelled)
            }
        }
    }

    private fun displaySettings() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.settings_fragment_container, CustomTimerSettingsFragment())
        transaction.commit()
    }

    private fun hideSettings() {
        supportFragmentManager.findFragmentById(R.id.settings_fragment_container)?.let { fragment ->
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.remove(fragment)
            transaction.commit()
        }
    }

    private fun displayChronometer() {
        if (!chronometerVisible) {
            val chronometerFragment = ChronometerFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
            transaction.replace(R.id.fragment_container, chronometerFragment)
            transaction.commit()
            chronometerVisible = true
        }
    }

    private fun hideChronometer() {
        if (chronometerVisible) {
            val chronometerFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (chronometerFragment != null) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
                transaction.remove(chronometerFragment)
                transaction.commit()
            }
            chronometerVisible = false
        }
    }

    private fun restartAlarm() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.kill() /* Kill any running alarms. */
        TimerUtils.startMainTimer(this)
        displayChronometer()
    }

    private fun sleep() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.sleep() /* Delete or kill any running alarm */
        currentAlarm?.let {
            TimerUtils.cancelAlarm(this, it.id)
            hideChronometer()
            Log.d(TAG, "Sleep mode engaged...")
        }
    }

    private fun snooze() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        currentAlarm?.let { alarm ->
            TimerUtils.startSnoozeTimer(this, alarm)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timer, menu) // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Application paused, chronometer stopped.")
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
        val STATE_CHECKED = "customTimerChecked"
    }

    private fun showSnackBar(v: View, message_id: Int) {
        val message = resources.getString(message_id)
        Snackbar.make(main_activity, message, Snackbar.LENGTH_LONG).run {
            view.setBackgroundColor(getColor(R.color.colorPrimary))
            show()
        }
    }
}
