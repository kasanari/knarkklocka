package se.jakob.knarkklocka.adapters

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("isGone")
fun bindIsGone(view: View, isGone: Boolean) {
    view.visibility =
        if (isGone) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
}