package se.jakob.knarkklocka;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.util.Locale;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class TimerLengthPreference extends DialogPreference {

    private TimePicker timePicker;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;

    private long length;

    public TimerLengthPreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimerLengthPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimerLengthPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
        setDialogLayoutResource(R.layout.dialog_numberpicker);
        //final Context themeContext = getContext();
        //final LayoutInflater inflater = LayoutInflater.from(themeContext);
        //final View view = inflater.inflate(R.layout.time_picker, null);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");

        length = 0;
    }

   /* @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext(), null, android.R.style.Theme_Holo_Light_Panel);
        picker.setIs24HourView(true);
        return (picker);
    }
*/
    @Override
    protected void onBindDialogView(View view) {
        hourPicker = view.findViewById(R.id.np_hours);
        minutePicker = view.findViewById(R.id.np_minutes);
        hourPicker.setMaxValue(100);
        hourPicker.setMinValue(0);
        minutePicker.setMaxValue(100);
        minutePicker.setMinValue(0);
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int hour = hourPicker.getValue();
            int minute = minutePicker.getValue();
            length = hour*HOUR_IN_MILLIS + minute*MINUTE_IN_MILLIS;

            setSummary(getSummary());

            if (callChangeListener(length)) {
                persistLong(length);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            if (defaultValue == null) {
                length = getPersistedLong(System.currentTimeMillis());
            } else {
                length = Long.parseLong(getPersistedString((String) defaultValue));
            }
        } else {
            if (defaultValue == null) {
                length = System.currentTimeMillis();
            } else {
                length = Long.parseLong((String) defaultValue);
            }
        }
        setSummary(getSummary());
    }

    public void setValue(long value) {
        this.length = value;
        persistLong(this.length);
    }

    public long getValue() {
        return this.length;
    }

    @Override
    public CharSequence getSummary() {
        int hours = (int)(length / HOUR_IN_MILLIS);
        int minutes = (int) (length / MINUTE_IN_MILLIS % 60);
        return String.format(Locale.getDefault(),"%d hours and %d minutes", hours, minutes);
    }
}
