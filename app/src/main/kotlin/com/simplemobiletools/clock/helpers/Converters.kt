package com.simplemobiletools.clock.helpers

import androidx.room.TypeConverter
import com.simplemobiletools.clock.extensions.gson.gson
import com.simplemobiletools.clock.models.StateWrapper
import com.simplemobiletools.clock.models.TimerState

class Converters {

    @TypeConverter
    fun jsonToTimerState(value: String): TimerState {
        return try {
            gson.fromJson(value, StateWrapper::class.java).state
        } catch (e: Exception) {
            TimerState.Idle
        }
    }

    @TypeConverter
    fun timerStateToJson(state: TimerState) = gson.toJson(StateWrapper(state))
}
