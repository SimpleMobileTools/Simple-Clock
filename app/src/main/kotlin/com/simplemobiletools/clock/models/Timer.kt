package com.simplemobiletools.clock.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class Timer(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val seconds: Int,
    val state: TimerState,
    val vibrate: Boolean,
    val soundUri: String,
    val soundTitle: String,
    val label: String,
    val createdAt: Long,
    val channelId: String? = null,
)
