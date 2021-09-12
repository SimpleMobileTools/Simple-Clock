package com.simplemobiletools.clock.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class Timer(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    var seconds: Int,
    val state: TimerState,
    var vibrate: Boolean,
    var soundUri: String,
    var soundTitle: String,
    var label: String,
    var createdAt: Long,
    var channelId: String? = null,
)
