package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class TimelineEvent(
    val status: String,
    val time: String,
    val note: String
)

@Entity(tableName = "shipments")
data class Shipment(
    @PrimaryKey
    val id: String, // e.g. "CF-101"
    val clientName: String,
    val clientType: String,
    val goods: String,
    val bay: String,
    val quantity: Int,
    val destination: String,
    val driver: String,
    val vehicle: String,
    val status: String, // "Assigned", "At Loading Bay", "In Transit", "Delivered"
    val createdAt: String,
    val deliveredAt: String? = null,
    val deliveryNote: String? = null,
    val timelineJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun parseTimeline(): List<TimelineEvent> {
        val list = mutableListOf<TimelineEvent>()
        try {
            val arr = JSONArray(timelineJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TimelineEvent(
                        status = obj.optString("status", ""),
                        time = obj.optString("time", ""),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    companion object {
        fun buildTimelineJson(events: List<TimelineEvent>): String {
            val arr = JSONArray()
            for (ev in events) {
                val obj = JSONObject()
                obj.put("status", ev.status)
                obj.put("time", ev.time)
                obj.put("note", ev.note)
                arr.put(obj)
            }
            return arr.toString()
        }
    }
}
