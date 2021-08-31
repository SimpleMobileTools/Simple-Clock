package com.simplemobiletools.clock.helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.simplemobiletools.clock.models.StateWrapper
import com.simplemobiletools.clock.models.TimerState

class Converters {
    private val gson = Gson()

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
