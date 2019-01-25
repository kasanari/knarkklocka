package se.jakob.knarkklocka.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.data.Alarm
import se.jakob.knarkklocka.data.AlarmState
import se.jakob.knarkklocka.viewmodels.AlarmViewModel
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.controller_fragment.*
import se.jakob.knarkklocka.AlarmBroadcasts
import se.jakob.knarkklocka.utils.*

class ControllerFragment : Fragment() {

    private lateinit var model: AlarmViewModel

    interface OnControllerEventListener {
        fun onControllerEvent(v: View, event: String)
    }

    private var mListener: OnControllerEventListener? = null

    private fun registerButtonListeners(view: View) {

        val fabStartTimer = view.findViewById<FloatingActionButton?>(R.id.fab_start_timer)
        val buttonRemoveTimer = view.findViewById<Button?>(R.id.button_remove_timer)
        val buttonSnoozeTimer = view.findViewById<Button?>(R.id.button_snooze_timer)

        fabStartTimer?.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(activity!!)
            mListener?.onControllerEvent(v, ACTION_RESTART_ALARM)
            true
        }

        buttonRemoveTimer?.setOnLongClickListener { v ->
            Klaxon.vibrateOnce(activity!!)
            mListener?.onControllerEvent(v, ACTION_SLEEP)
            true
        }

        buttonSnoozeTimer?.setOnClickListener { v ->
            mListener?.onControllerEvent(v, ACTION_SNOOZE_ALARM)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as? OnControllerEventListener
        if (mListener == null) {
            throw ClassCastException("$context must implement OnControllerEventListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.controller_fragment, container, false)
        registerButtonListeners(rootView)
        subscribeUI()
        return rootView
    }

    fun subscribeUI() {
        val factory = InjectorUtils.provideMainActivityViewModelFactory(requireContext())
        model = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)

        /*model.liveAlarm.observe(viewLifecycleOwner, Observer<Alarm> { alarm ->
            alarm?.run {
                when (alarm.state) {
                    AlarmState.STATE_WAITING -> TODO()
                    AlarmState.STATE_DEAD -> TODO()
                    AlarmState.STATE_ACTIVE -> TODO()
                    AlarmState.STATE_SNOOZING -> TODO()
                    AlarmState.STATE_MISSED -> TODO()
                }
            }
        })*/
    }
}
