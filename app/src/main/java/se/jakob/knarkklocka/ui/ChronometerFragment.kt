package se.jakob.knarkklocka.ui

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.viewmodels.AlarmViewModel
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel


class ChronometerFragment : Fragment() {

    private lateinit var model: AlarmViewModel
    lateinit var chronometer : Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = activity?.run {
            ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        model.liveAlarm.observe(this, Observer<Alarm> { alarm ->
            alarm?.run {
                val timeDelta = alarm.endTime.time - System.currentTimeMillis()
                chronometer.run {
                    base = SystemClock.elapsedRealtime() + timeDelta
                    start()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.chronometer_fragment, container, false)
        chronometer = rootView.findViewById(R.id.chronometer_main)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        chronometer.start()
    }

    override fun onStop() {
        chronometer.stop()
        super.onStop()
    }
}