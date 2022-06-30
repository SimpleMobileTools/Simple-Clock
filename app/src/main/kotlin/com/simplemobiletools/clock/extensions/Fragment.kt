package com.simplemobiletools.clock.extensions

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager

val Fragment.requiredActivity: FragmentActivity get() = requireActivity()

val Fragment.preferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(requiredActivity)

val Fragment.isReady: Boolean get() = activity != null && context != null && isAdded
