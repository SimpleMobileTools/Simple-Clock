package com.simplemobiletools.clock.extensions

import android.widget.TextView
import com.simplemobiletools.commons.extensions.applyColorFilter

fun TextView.colorCompoundDrawable(color: Int) {
    compoundDrawables.filterNotNull().forEach { drawable ->
        drawable.applyColorFilter(color)
        setCompoundDrawables(drawable, null, null, null)
    }
}
