package se.jakob.knarkklocka.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

import se.jakob.knarkklocka.R;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.WordViewHolder> {

    private final LayoutInflater mInflater;
    private List<Alarm> mAlarms; // Cached copy of alarms

    AlarmListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new WordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        if (mAlarms != null) {
            Alarm current = mAlarms.get(position);
            DateFormat dateFormat = DateFormat.getTimeInstance();
            String dateString = dateFormat.format(current.getEndTime());
            holder.alarmItemView.setText(dateString);
        } else {
            // Covers the case of data not being ready yet.
            holder.alarmItemView.setText("No Alarm");
        }
    }

    void setWords(List<Alarm> words) {
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

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView alarmItemView;

        private WordViewHolder(View itemView) {
            super(itemView);
            alarmItemView = itemView.findViewById(R.id.textView);
        }
    }
}