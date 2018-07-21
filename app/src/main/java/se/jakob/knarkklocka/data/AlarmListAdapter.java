package se.jakob.knarkklocka.data;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import se.jakob.knarkklocka.R;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder> {

    private final LayoutInflater mInflater;
    private List<Alarm> mAlarms; // Cached copy of alarms
    private Resources res;

    public AlarmListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        res = context.getResources();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        if (mAlarms != null) {
            Alarm current = mAlarms.get(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String dateString = dateFormat.format(current.getEndTime());
            String string = current.getId() + " " + dateString;
            holder.dueTimeItemView.setText(string);

            int state = current.getState();
            String stateString = current.getStateString(state);
            holder.stateItemView.setText(stateString);


            int snoozes = current.getSnoozes();
            String snoozeString = res.getQuantityString(R.plurals.snoozes, snoozes, snoozes);
            //String snoozeString = "Snoozes: " + snoozes;
            holder.snoozeItemView.setText(snoozeString);
        } else {
            // Covers the case of data not being ready yet.
            holder.dueTimeItemView.setText("No Alarms");
        }
    }

    public void setAlarms(List<Alarm> words) {
        mAlarms = words;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mAlarms has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mAlarms != null)
            return mAlarms.size();
        else return 0;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private final TextView dueTimeItemView;
        private final TextView stateItemView;
        private final TextView snoozeItemView;

        private AlarmViewHolder(View itemView) {
            super(itemView);
            dueTimeItemView = itemView.findViewById(R.id.tv_alarm_due);
            stateItemView = itemView.findViewById(R.id.tv_alarm_state);
            snoozeItemView = itemView.findViewById(R.id.tv_alarm_snoozes);
        }
    }
}