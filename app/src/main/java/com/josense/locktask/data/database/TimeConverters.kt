package com.josense.locktask.data.database

import androidx.room.TypeConverter
import java.util.Date

class TimeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
