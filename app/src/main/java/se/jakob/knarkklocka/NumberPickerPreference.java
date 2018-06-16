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
public class NumberPickerPreference extends DialogPreference {

    private TimePicker picker = null;

    private long length;

    public NumberPickerPreference(Context ctxt) {
        this(ctxt, null);
    }

    public NumberPickerPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public NumberPickerPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        length = 0;
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int hour = picker.getHour();
            int minute = picker.getMinute();
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
