package com.my.mapupassessment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Double = 0.0,
    val duration: Long = 0L
)
