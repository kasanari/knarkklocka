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
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import se.jakob.knarkklocka.utils.TimerUtils.doBackgroundWork
import se.jakob.knarkklocka.utils.TimerUtils.getAlarmActionIntent
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel

class TimerActivity : AppCompatActivity(), ControllerFragment.OnControllerEventListener {

    private lateinit var viewModel: MainActivityViewModel

    private var currentAlarm: Alarm? = null

    private var chronometerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timer_main)

        hideChronometer() // Hide chronometer initially, it will be opened if there is an alarm running

        Utils.checkIfWhiteListed(this) // Check if the app is ignoring battery saving optimizations

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true) // Set the default preference values

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(MainActivityViewModel::class.java)

        viewModel.liveAlarm.observe(this, Observer { alarm ->
            if (alarm != null) {
                currentAlarm = alarm
                when (alarm.state) {
                    STATE_ACTIVE -> {
                        displayChronometer()
                    }
                    STATE_DEAD -> {
                        hideChronometer()
                    }
                    STATE_SNOOZING -> {
                        displayChronometer()
                    }
                    STATE_WAITING -> {
                        displayChronometer()
                    }
                    STATE_MISSED -> {
                        displayChronometer()
                    }
                }
            } else {
                hideChronometer()
            }
        })

        /* Setting up a Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.timeractivity_title)
        setSupportActionBar(toolbar)

    }

    /** Callback function for the [ControllerFragment] **/
    override fun onControllerEvent(v: View, event: String) {
        Klaxon.vibrateOnce(this)
        when (event) {
            ACTION_RESTART -> {
                currentAlarm?.run {
                    if (this.state == STATE_DEAD) {
                        startAlarm()
                    } else {
                        restartAlarm(this)
                    }
                } ?: startAlarm()

            }
            ACTION_SNOOZE -> {
                currentAlarm?.run {
                    snooze(this)
                }
                showSnackBar(R.string.snackbar_alarm_snoozed)
            }
            ACTION_SLEEP -> {
                currentAlarm?.run {
                    sleep(this)
                }
                showSnackBar(R.string.snackbar_alarm_cancelled)
            }
        }
    }

    /** Display the countdown timer at the top of the screen **/
    private fun displayChronometer() {
            val settingsFragment = supportFragmentManager.findFragmentById(R.id.settings_fragment_container)
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_top, R.anim.abc_fade_out)
                setReorderingAllowed(true)
                if (!chronometerVisible) {
                    val chronometerFragment = ChronometerFragment()
                    replace(R.id.fragment_container, chronometerFragment)
                }
                settingsFragment?.run { remove(settingsFragment) }
            }
            chronometerVisible = true
        }

    /** Hide the countdown timer at the top of the screen **/
    private fun hideChronometer() {
            val chronometerFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                supportFragmentManager.commit {
                    setTransition(TRANSIT_FRAGMENT_FADE)
                    setReorderingAllowed(true)
                    if (chronometerVisible) {
                        chronometerFragment?.run { remove(chronometerFragment) }
                        chronometerVisible = false
                    }
                    replace(R.id.settings_fragment_container, CustomTimerSettingsFragment())
                }
    }

    /** Kill the current alarm and create a new one **/
    private fun restartAlarm(alarm: Alarm) {
        doBackgroundWork(this, ACTION_RESTART, alarm.id)
        showSnackBar(R.string.snackbar_alarm_restarted)
    }


    private fun startAlarm() {
        doBackgroundWork(this, ACTION_RESTART, -1)
        showSnackBar(R.string.snackbar_alarm_created)
    }

    /** Kill the current alarm **/
    private fun sleep(alarm: Alarm) {
        doBackgroundWork(this, ACTION_SLEEP, alarm.id)
        hideChronometer()
        Log.d(TAG, "Sleep mode engaged...")
    }

    /** Snooze the current alarm; that is, add the current snooze interval to the current alarm **/
    private fun snooze(alarm: Alarm) {
        doBackgroundWork(this, ACTION_SNOOZE, alarm.id)
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

        return when (item.itemId) {
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

    private fun showSnackBar(message_id: Int) {
        val message = resources.getString(message_id)
        Snackbar.make(main_activity, message, Snackbar.LENGTH_LONG).run {
            view.setBackgroundColor(getColor(R.color.colorPrimary))
            show()
        }
    }
}
