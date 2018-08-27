package se.jakob.knarkklocka.data

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Locale

import se.jakob.knarkklocka.R

class AlarmListAdapter(context: Context) : RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder>() {

    private val mInflater: LayoutInflater
    private var mAlarms: List<Alarm>? = null // Cached copy of alarms
    private val res: Resources

    init {
        mInflater = LayoutInflater.from(context)
        res = context.resources
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false)
        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        if (mAlarms != null) {
            val current = mAlarms!![position]
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateString = dateFormat.format(current.endTime)
            val string = current.id.toString() + " " + dateString
            holder.dueTimeItemView.text = string

            val stateString = current.stateToString
            holder.stateItemView.text = stateString


            val snoozes = current.snoozes
            val snoozeString = res.getQuantityString(R.plurals.snoozes, snoozes, snoozes)
            //String snoozeString = "Snoozes: " + snoozes;
            holder.snoozeItemView.text = snoozeString
        } else {
            // Covers the case of data not being ready yet.
            holder.dueTimeItemView.text = "No Alarms"
        }
    }

    fun setAlarms(words: List<Alarm>) {
        mAlarms = words
        notifyDataSetChanged()
    }

    // getItemCount() is called many times, and when it is first called,
    // mAlarms has not been updated (means initially, it's null, and we can't return null).
    override fun getItemCount(): Int {
        return if (mAlarms != null)
            mAlarms!!.size
        else
            0
    }

    internal inner class AlarmViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dueTimeItemView: TextView
        private val stateItemView: TextView
        private val snoozeItemView: TextView

        init {
            dueTimeItemView = itemView.findViewById(R.id.tv_alarm_due)
            stateItemView = itemView.findViewById(R.id.tv_alarm_state)
            snoozeItemView = itemView.findViewById(R.id.tv_alarm_snoozes)
        }
    }
}