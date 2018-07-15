package se.jakob.knarkklocka;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Chronometer;
import android.widget.TimePicker;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long duration = PreferenceUtils.getCustomTimerLength(getActivity());
        int hours = (int)(duration / HOUR_IN_MILLIS);
        int minutes = (int) (duration / MINUTE_IN_MILLIS % 60);
        return new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Panel, this, hours, minutes, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        long hoursInMillis = hourOfDay * HOUR_IN_MILLIS;
        long minutesInMillis = minute * MINUTE_IN_MILLIS;
        long duration = (hoursInMillis + minutesInMillis);
        PreferenceUtils.setCustomTimerLength(getActivity(), (int) duration);
        Chronometer chronometer = getActivity().findViewById(R.id.time_display);
        if (chronometer != null) {
            chronometer.setBase(SystemClock.elapsedRealtime() + duration);
        }

    }
}
