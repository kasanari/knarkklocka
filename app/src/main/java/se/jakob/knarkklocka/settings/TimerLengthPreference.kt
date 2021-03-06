package se.jakob.knarkklocka.settings

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.AttributeSet
import androidx.preference.DialogPreference
import se.jakob.knarkklocka.R

/**
 * A [android.preference.Preference] that displays a number picker as a dialog.
 */
class TimerLengthPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = android.R.attr.dialogPreferenceStyle)
    : DialogPreference(context, attrs, defStyle) {

    var time: Long = 0
        set(value) {
            field = value
            persistLong(value)
            notifyChanged()
        }

    val wholeMinutes: Int
        get() {
            return (time / MINUTE_IN_MILLIS % 60).toInt()
        }

    val wholeHours: Int
        get() {
            return (time / HOUR_IN_MILLIS).toInt()
        }

    init {
        dialogLayoutResource = R.layout.dialog_numberpicker
        setPositiveButtonText(R.string.positive)
        setNegativeButtonText(R.string.negative)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        time = if (defaultValue == null) {
            getPersistedLong(5 * MINUTE_IN_MILLIS)
        } else {
            getPersistedLong(defaultValue.toString().toLong())
        }
    }

    override fun getSummary(): CharSequence {
        val hours = wholeHours
        val minutes = wholeMinutes
        var summary = ""

        if (hours > 0) {
            val hoursString = context.resources.getQuantityString(R.plurals.hours, hours, hours)
            summary += hoursString
            if (minutes > 0) {
                summary += " and "
            }
        }
        if (minutes > 0) {
            val minutesString = context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
            summary += minutesString
        }

        return summary
    }
}