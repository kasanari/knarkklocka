package se.jakob.knarkklocka.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import se.jakob.knarkklocka.R
import se.jakob.knarkklocka.databinding.ControllerFragmentBinding
import se.jakob.knarkklocka.utils.ACTION_RESTART
import se.jakob.knarkklocka.utils.ACTION_SLEEP
import se.jakob.knarkklocka.utils.ACTION_SNOOZE
import se.jakob.knarkklocka.utils.InjectorUtils
import se.jakob.knarkklocka.viewmodels.MainActivityViewModel

/**
 * [Fragment] which stores buttons that control the timer. The buttons can start, snooze and cancel timers.
 */
class ControllerFragment : Fragment() {

    private val alarmViewModel : MainActivityViewModel by activityViewModels {
        InjectorUtils.provideMainActivityViewModelFactory(requireActivity())
    }

    interface OnControllerEventListener {
        fun onControllerEvent(v: View, event: String)
    }

    private var mListener: OnControllerEventListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as? OnControllerEventListener
        if (mListener == null) {
            throw ClassCastException("$context must implement OnControllerEventListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<ControllerFragmentBinding>(
                inflater, R.layout.controller_fragment, container, false).apply {
            viewModel = alarmViewModel
            lifecycleOwner = this@ControllerFragment
            buttonStartTimer.setOnClickListener { v ->
                mListener?.onControllerEvent(v, ACTION_RESTART)
            }
            buttonRemoveTimer.setOnClickListener { v ->
                mListener?.onControllerEvent(v, ACTION_SLEEP)
            }
            buttonSnoozeTimer.setOnClickListener { v ->
                mListener?.onControllerEvent(v, ACTION_SNOOZE)
            }
        }

        return binding.root
    }
}
