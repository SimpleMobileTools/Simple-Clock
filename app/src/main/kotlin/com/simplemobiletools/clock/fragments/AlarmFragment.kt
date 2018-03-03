package com.simplemobiletools.clock.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.clock.R
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.fragment_alarm.view.*

class AlarmFragment : Fragment() {
    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        view = inflater.inflate(R.layout.fragment_alarm, container, false) as ViewGroup
        return view
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    private fun setupViews() {
        view.apply {
            context!!.updateTextColors(alarm_fragment)
            alarm_fab.setOnClickListener {
                fabClicked()
            }
        }
    }

    private fun fabClicked() {

    }
}
