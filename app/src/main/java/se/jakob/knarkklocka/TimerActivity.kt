package se.jakob.knarkklocka

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState.*
import se.jakob.knarkklocka.settings.SettingsActivity
import se.jakob.knarkklocka.utils.*
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel
import java.util.*

class TimerActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private var currentAlarm: Alarm? = null
    private var chronometer : Chronometer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timer_main)

        AlarmNotificationsUtils.setupChannels(this)
        
        Utils.checkIfWhiteListed(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)

        val mSceneRoot: ViewGroup = findViewById(R.id.scene_root)
        val deadScene: Scene = Scene.getSceneForLayout(mSceneRoot, R.layout.timer_dead, this)
        val waitingScene: Scene = Scene.getSceneForLayout(mSceneRoot, R.layout.timer_waiting, this)
        val activeScene: Scene = Scene.getSceneForLayout(mSceneRoot, R.layout.timer_active, this)
        val snoozeScene: Scene = Scene.getSceneForLayout(mSceneRoot, R.layout.timer_snooze, this)
        val mFadeTransition: Transition = Fade()

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        viewModel.liveAlarm.observe(this, Observer { alarm ->
            if (alarm != null) {
                currentAlarm = alarm
                val state = alarm.state
                when (state) {
                    STATE_ACTIVE -> {
                        TransitionManager.go(activeScene, mFadeTransition)
                    }
                    STATE_DEAD -> {
                        TransitionManager.go(deadScene, mFadeTransition)
                    }
                    STATE_SNOOZING -> {
                        TransitionManager.go(snoozeScene, mFadeTransition)
                    }
                    STATE_WAITING -> {
                        TransitionManager.go(waitingScene, mFadeTransition)
                    }
                    STATE_MISSED -> {
                        TransitionManager.go(activeScene, mFadeTransition)
                    }
                }
                registerButtonListeners()
                setupChronometer(alarm.endTime)
            } else {
                TransitionManager.go(deadScene, mFadeTransition)
            }
        })

        /* Setting up Toolbar instead of ActionBar */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Timer control"
        setSupportActionBar(toolbar)

    }

    private fun registerButtonListeners() {

        val fabStartTimer = findViewById<FloatingActionButton?>(R.id.fab_start_timer)
        val buttonRemoveTimer = findViewById<Button?>(R.id.button_remove_timer)
        val buttonSnoozeTimer = findViewById<Button?>(R.id.button_snooze_timer)

        fabStartTimer?.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(this)
            restartAlarm()
            Snackbar.make(v, "Started new timer!", Snackbar.LENGTH_LONG).show()
            true
        }

        buttonRemoveTimer?.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(this)
            sleep(v)
            true
        }

        buttonSnoozeTimer?.setOnClickListener { v -> snooze(v) }
    }

    private fun setupChronometer(endTime: Date) {
        val timeDelta = endTime.time - System.currentTimeMillis()
        findViewById<Chronometer?>(R.id.chronometer_main)?.run {
            base = SystemClock.elapsedRealtime() + timeDelta
            start()
        }
    }

    private fun restartAlarm() {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.kill() /* Kill any running alarms. */
        setupChronometer(TimerUtils.startMainTimer(this))
    }

    private fun sleep(v: View) {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        viewModel.sleep() /* Delete or kill any running alarm */
        currentAlarm?.let {
            TimerUtils.cancelAlarm(this, it.id)
            Log.d(TAG, "Sleep mode engaged...")
            Snackbar.make(v, "Goodnight", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun snooze(v: View) {
        AlarmBroadcasts.broadcastAlarmHandled(this)
        currentAlarm?.let {alarm ->
            setupChronometer(TimerUtils.startSnoozeTimer(this, alarm))
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
            chronometer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        chronometer?.stop()
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
    }
}
