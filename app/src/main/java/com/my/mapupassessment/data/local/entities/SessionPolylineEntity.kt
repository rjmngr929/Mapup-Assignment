package com.my.mapupassessment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_polylines")
data class SessionPolylineEntity(
    @PrimaryKey
    val sessionId: Long,   // one session = one polyline

    val encodedPolyline: String,

    val updatedAt: Long
)
