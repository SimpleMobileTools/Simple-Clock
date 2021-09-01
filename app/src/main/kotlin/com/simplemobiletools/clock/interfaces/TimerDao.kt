package com.simplemobiletools.clock.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplemobiletools.clock.models.Timer

@Dao
interface TimerDao {

    @Query("SELECT * FROM timers ORDER BY createdAt DESC")
    fun getTimers(): List<Timer>

    @Query("SELECT * FROM timers WHERE id=:id")
    fun getTimer(id: Long): Timer

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateTimer(timer: Timer): Long

    @Query("DELETE FROM timers WHERE id=:id")
    fun deleteTimer(id: Long)
}
