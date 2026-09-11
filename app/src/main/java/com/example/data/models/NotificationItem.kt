package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val time: String,
    val type: String, // "transit", "bay", "delivered", "dispatch", "info"
    val timestamp: Long = System.currentTimeMillis()
)
