package se.jakob.knarkklocka.settings

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.PreferenceManager
import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.AttributeSet
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
            notifyChanged()
            persistLong(this.time)
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