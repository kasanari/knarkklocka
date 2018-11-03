package se.jakob.knarkklocka.data
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import se.jakob.knarkklocka.R
import java.text.SimpleDateFormat
import java.util.*

class AlarmListAdapter(context: Context) : RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    var mAlarms: List<Alarm>? = null // Cached copy of alarms
    set(alarms) {
        field = alarms
        notifyDataSetChanged()
    }
    private val res: Resources = context.resources

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false)
        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        mAlarms?.run {
            val currentAlarm = this[position]
            val dateString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentAlarm.endTime)
            //val dateString = dateFormat.format(currentLiveAlarm.endTime)
            //val string = currentLiveAlarm.id.toString() + " " + dateString
            holder.dueTimeItemView.text = String.format(Locale.getDefault(), "%d: \t %s", currentAlarm.id, dateString)

            val stateString = currentAlarm.stateToString
            holder.stateItemView.text = stateString

            val snoozes = currentAlarm.snoozes
            val snoozeString = res.getQuantityString(R.plurals.snoozes, snoozes, snoozes)
            //String snoozeString = "Snoozes: " + snoozes;
            holder.snoozeItemView.text = snoozeString
        } ?: run {
            // Covers the case of data not being ready yet.
            holder.dueTimeItemView.text = res.getString(R.string.viewholder_no_alarms)
        }
    }

    // getItemCount() is called many times, and when it is first called,
    // mAlarms has not been updated (means initially, it's null, and we can't return null).
    override fun getItemCount(): Int {
        return mAlarms?.size ?: 0
    }

    inner class AlarmViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dueTimeItemView: TextView = itemView.findViewById(R.id.tv_alarm_due)
        val stateItemView: TextView = itemView.findViewById(R.id.tv_alarm_state)
        val snoozeItemView: TextView = itemView.findViewById(R.id.tv_alarm_snoozes)
    }
}