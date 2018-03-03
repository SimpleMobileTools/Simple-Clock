package com.simplemobiletools.clock.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.simplemobiletools.clock.models.Alarm

class DBHelper private constructor(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val ALARMS_TABLE_NAME = "contacts"
    private val COL_ID = "id"
    private val COL_TIME_IN_MINUTES = "time_in_minutes"
    private val COL_DAYS = "days"
    private val COL_IS_ENABLED = "is_enabled"
    private val COL_VIBRATE = "vibrate"
    private val COL_SOUND_URI = "sound_uri"
    private val COL_LABEL = "label"

    private val mDb = writableDatabase

    companion object {
        private const val DB_VERSION = 1
        const val DB_NAME = "alarms.db"
        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null)
                dbInstance = DBHelper(context)

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $ALARMS_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TIME_IN_MINUTES INTEGER, $COL_DAYS INTEGER, " +
                "$COL_IS_ENABLED INTEGER, $COL_VIBRATE INTEGER, $COL_SOUND_URI TEXT, $COL_LABEL TEXT)")
        insertInitialAlarms(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun insertInitialAlarms(db: SQLiteDatabase) {
        val weekDays = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT
        val weekDaysAlarm = Alarm(420, weekDays, false, false, "", "")
        insertAlarm(weekDaysAlarm, db)

        val weekEnd = SATURDAY_BIT or SUNDAY_BIT
        val weekEndAlarm = Alarm(540, weekEnd, false, false, "", "")
        insertAlarm(weekEndAlarm, db)
    }

    private fun insertAlarm(alarm: Alarm, db: SQLiteDatabase) {
        val values = fillAlarmContentValues(alarm)
        db.insert(ALARMS_TABLE_NAME, null, values)
    }

    private fun fillAlarmContentValues(alarm: Alarm): ContentValues {
        return ContentValues().apply {
            put(COL_TIME_IN_MINUTES, alarm.timeInMinutes)
            put(COL_DAYS, alarm.days)
            put(COL_IS_ENABLED, alarm.isEnabled)
            put(COL_VIBRATE, alarm.vibrate)
            put(COL_SOUND_URI, alarm.soundUri)
            put(COL_LABEL, alarm.label)
        }
    }
}
