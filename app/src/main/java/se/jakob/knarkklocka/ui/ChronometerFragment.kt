package se.jakob.knarkklocka.ui

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.databinding.ChronometerFragmentBinding
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.viewmodels.AlarmViewModel
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel


class ChronometerFragment : Fragment() {

    private lateinit var model: AlarmViewModel
    lateinit var chronometer : Chronometer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val factory = InjectorUtils.provideMainActivityViewModelFactory(requireActivity())
        model = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        val binding = DataBindingUtil.inflate<ChronometerFragmentBinding>(
                inflater, R.layout.chronometer_fragment, container, false).apply {
            viewModel = model as MainActivityViewModel
            setLifecycleOwner(this@ChronometerFragment)
        }

        chronometer = binding.chronometerMain

        model.liveAlarm.observe(this, Observer<Alarm> { alarm ->
            alarm?.run {
                val timeDelta = alarm.endTime.time - System.currentTimeMillis()
                chronometer.run {
                    base = SystemClock.elapsedRealtime() + timeDelta
                    start()
                }
            }
        })

        return binding.root
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