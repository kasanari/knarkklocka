package se.jakob.knarkklocka

import android.os.Bundle

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_history.*
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmListAdapter
import se.jakob.knarkklocka.utils.AlarmNotificationsUtils
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.utils.TimerUtils
import se.jakob.knarkklocka.viewmodels.AlarmHistoryViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: AlarmHistoryViewModel

    private var currentAlarm: Alarm? = null

    private lateinit var adapter : AlarmListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        adapter = AlarmListAdapter(this)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider.
        val factory = InjectorUtils.provideAlarmHistoryViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(AlarmHistoryViewModel::class.java)

        viewModel.allAlarms.observe(this, Observer { alarms: List<Alarm> ->
            // Update the cached copy of the words in the adapter.
            adapter.mAlarms = alarms
        })

        viewModel.liveAlarm.observe(this, Observer { alarm -> currentAlarm = alarm })
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
        return when (item.itemId) {
            R.id.action_clear_history -> {
                if (viewModel.hasAlarm) {
                    TimerUtils.cancelAlarm(applicationContext, currentAlarm!!.id)
                    AlarmNotificationsUtils.clearAllNotifications(this)
                }
                adapter.notifyItemRangeRemoved(0, adapter.itemCount)
                viewModel.clearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
