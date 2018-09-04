package se.jakob.knarkklocka

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_history.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmListAdapter
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.viewmodels.AlarmHistoryViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var mAlarmHistoryViewModel: AlarmHistoryViewModel

    private var currentAlarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val adapter = AlarmListAdapter(this)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider.
        val factory = InjectorUtils.provideAlarmHistoryViewModelFactory(this)
        mAlarmHistoryViewModel = ViewModelProviders.of(this, factory).get(AlarmHistoryViewModel::class.java)

        mAlarmHistoryViewModel.allAlarms?.observe(this, Observer { alarms ->
            // Update the cached copy of the words in the adapter.
            adapter.mAlarms = alarms
        })

        mAlarmHistoryViewModel.liveAlarm.observe(this, Observer { alarm -> currentAlarm = alarm })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return when (id) {
            R.id.action_clear_history -> {
                if (mAlarmHistoryViewModel.alarmIsRunning) {
                    TimerUtils.cancelAlarm(applicationContext, currentAlarm!!.id)
                    AlarmNotificationsUtils.clearAllNotifications(this)
                }

                mAlarmHistoryViewModel.clearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
