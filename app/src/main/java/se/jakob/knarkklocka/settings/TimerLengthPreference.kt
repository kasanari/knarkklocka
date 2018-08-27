package se.jakob.knarkklocka.settings

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker
import android.widget.TimePicker

import se.jakob.knarkklocka.R

import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS

/**
 * A [android.preference.Preference] that displays a number picker as a dialog.
 */
class TimerLengthPreference @JvmOverloads constructor(
        ctxt: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = android.R.attr.dialogPreferenceStyle)
    : DialogPreference(ctxt, attrs, defStyle) {

    private val timePicker: TimePicker? = null
    private var hourPicker: NumberPicker? = null
    private var minutePicker: NumberPicker? = null

    private var length: Long = 0

    var value: Long
        get() = this.length
        set(value) {
            this.length = value
            persistLong(this.length)
        }

    init {
        dialogLayoutResource = R.layout.dialog_numberpicker
        setPositiveButtonText(R.string.positive)
        setNegativeButtonText(R.string.negative)
        summary = summary
        length = 0
    }

    private fun setupPickers(view: View) {
        hourPicker = view.findViewById(R.id.np_hours)
        minutePicker = view.findViewById(R.id.np_minutes)
        hourPicker!!.maxValue = 100
        hourPicker!!.minValue = 0
        minutePicker!!.maxValue = 59
        minutePicker!!.minValue = 0
        hourPicker!!.wrapSelectorWheel = false
        minutePicker!!.wrapSelectorWheel = false
        hourPicker!!.value = getWholeHours(length)
        minutePicker!!.value = getWholeMinutes(length)
    }

    override fun onBindDialogView(view: View) {
        setupPickers(view)
        super.onBindDialogView(view)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        if (positiveResult) {
            val hour = hourPicker!!.value
            val minute = minutePicker!!.value
            val newLength = hour * HOUR_IN_MILLIS + minute * MINUTE_IN_MILLIS

            summary = summary

            if (callChangeListener(newLength)) {
                value = newLength
                notifyChanged()
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (restoreValue) {
            if (defaultValue == null) {
                length = getPersistedLong(System.currentTimeMillis())
            } else {
                length = java.lang.Long.parseLong(getPersistedString(defaultValue as String?))
            }
        } else {
            if (defaultValue == null) {
                length = System.currentTimeMillis()
            } else {
                length = java.lang.Long.parseLong((defaultValue as String?)!!)
            }
        }
        summary = summary
    }

    private fun getWholeMinutes(length: Long): Int {
        return (length / MINUTE_IN_MILLIS % 60).toInt()
    }

    private fun getWholeHours(length: Long): Int {
        return (length / HOUR_IN_MILLIS).toInt()
    }

    override fun getSummary(): CharSequence {
        val hours = getWholeHours(length)
        val minutes = getWholeMinutes(length)
        val res = context.resources
        var summary = ""

        if (hours > 0) {
            val hoursString = res.getQuantityString(R.plurals.hours, hours, hours)
            summary += hoursString
            if (minutes > 0) {
                summary += " and "
            }
        }
        if (minutes > 0) {
            val minutesString = res.getQuantityString(R.plurals.minutes, minutes, minutes)
            summary += minutesString
        }

        return summary
    }
}