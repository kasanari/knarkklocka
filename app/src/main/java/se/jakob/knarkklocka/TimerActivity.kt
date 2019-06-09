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
import androidx.fragment.app.commit
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

        AlarmNotificationsUtils.setupChannels(this) // Create notification channels

        Utils.checkIfWhiteListed(this) // Check if the app is ignoring battery saving optimizations

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true) // Set the default preference values

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

        /* Setting up a Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Timer control"
        setSupportActionBar(toolbar)

    }

    /** Callback function for the [ControllerFragment] **/
    override fun onControllerEvent(v: View, event: String) {
        when (event) {
            ACTION_RESTART_ALARM -> {
                restartAlarm()
                showSnackBar(R.string.snackbar_alarm_created)
            }
            ACTION_SNOOZE_ALARM -> {
                snooze()
                showSnackBar(R.string.snackbar_alarm_snoozed)
            }
            ACTION_SLEEP -> {
                sleep()
                showSnackBar(R.string.snackbar_alarm_cancelled)
            }
        }
    }

    /** Show the settings for creating a custom timer **/
    private fun displaySettings() {
        supportFragmentManager.commit {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.settings_fragment_container, CustomTimerSettingsFragment())
        }
    }

    /** Hide the settings for creating a custom timer **/
    private fun hideSettings() {
        supportFragmentManager.findFragmentById(R.id.settings_fragment_container)?.let { fragment ->
            supportFragmentManager.commit {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                remove(fragment)
            }
        }
    }

    /** Display the countdown timer at the top of the screen **/
    private fun displayChronometer() {
        if (!chronometerVisible) {
            val chronometerFragment = ChronometerFragment()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
                replace(R.id.fragment_container, chronometerFragment)
            }
            chronometerVisible = true
        }
    }

    /** Hide the countdown timer at the top of the screen **/
    private fun hideChronometer() {
        if (chronometerVisible) {
            val chronometerFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (chronometerFragment != null) {
                supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
                    remove(chronometerFragment)
                }
            }
            chronometerVisible = false
        }
    }

    /** Kill the current alarm and create a new one **/
    private fun restartAlarm() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.kill() /* Kill any running alarms. */
        TimerUtils.startMainTimer(this)
        displayChronometer()
    }

    /** Kill the current alarm **/
    private fun sleep() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.sleep() /* Delete or kill any running alarm */
        currentAlarm?.let {
            TimerUtils.cancelAlarm(this, it.id)
            hideChronometer()
            Log.d(TAG, "Sleep mode engaged...")
        }
    }

    /** Snooze the current alarm; that is, add the current snooze interval to the current alarm **/
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
        const val STATE_CHECKED = "customTimerChecked"
    }

    private fun showSnackBar(message_id: Int) {
        val message = resources.getString(message_id)
        Snackbar.make(main_activity, message, Snackbar.LENGTH_LONG).run {
            view.setBackgroundColor(getColor(R.color.colorPrimary))
            show()
        }
    }
}
