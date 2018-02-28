package com.simplemobiletools.clock.extensions

import android.content.Context
import com.simplemobiletools.clock.helpers.Config

val Context.config: Config get() = Config.newInstance(applicationContext)
