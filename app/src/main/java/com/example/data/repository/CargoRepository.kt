package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.models.MasterData
import com.example.data.models.NotificationItem
import com.example.data.models.Shipment
import com.example.data.models.TimelineEvent
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CargoRepository(private val db: AppDatabase) {
    private val shipmentDao = db.shipmentDao()
    private val notificationDao = db.notificationDao()

    val allShipments: Flow<List<Shipment>> = shipmentDao.getAllShipments()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()

    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun initializePreloadedDataIfNeeded() {
        val count = shipmentDao.getShipmentCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val demoShipments = listOf(
                Shipment(
                    id = "CF-101",
                    clientName = "Skyline Infrastructures",
                    clientType = "Organization",
                    goods = "Glass Tuff",
                    bay = "Bay No. 1",
                    quantity = 40,
                    destination = "Sector 54 Golf Course Road, Cyber City",
                    driver = "Sam",
                    vehicle = "Ashok Leyland Dost",
                    status = "In Transit",
                    createdAt = "10:15 AM",
                    timelineJson = Shipment.buildTimelineJson(
                        listOf(
                            TimelineEvent("Assigned", "10:15 AM", "Order dispatched by Admin"),
                            TimelineEvent("At Loading Bay", "10:28 AM", "Sam docked at Bay No. 1"),
                            TimelineEvent("In Transit", "10:45 AM", "Cargo inspected & in transit")
                        )
                    ),
                    timestamp = now - 1000 * 60 * 30
                ),
                Shipment(
                    id = "CF-102",
                    clientName = "Defence Research Estate",
                    clientType = "Government Entity",
                    goods = "Glass Bullet Proof",
                    bay = "Bay No. 2",
                    quantity = 12,
                    destination = "Block C, DRDO Complex, South Wing",
                    driver = "Ram",
                    vehicle = "Tata Yodha",
                    status = "At Loading Bay",
                    createdAt = "11:00 AM",
                    timelineJson = Shipment.buildTimelineJson(
                        listOf(
                            TimelineEvent("Assigned", "11:00 AM", "Priority defense dispatch"),
                            TimelineEvent("At Loading Bay", "11:20 AM", "Ram arrived at Bay No. 2")
                        )
                    ),
                    timestamp = now - 1000 * 60 * 20
                ),
                Shipment(
                    id = "CF-103",
                    clientName = "Verma Residency Renovation",
                    clientType = "Individual Buyer",
                    goods = "Window Frames",
                    bay = "Bay No. 3",
                    quantity = 15,
                    destination = "Villa 14, Rosewood Enclave, Phase 2",
                    driver = "Manav",
                    vehicle = "Tata Intra",
                    status = "Assigned",
                    createdAt = "11:30 AM",
                    timelineJson = Shipment.buildTimelineJson(
                        listOf(
                            TimelineEvent("Assigned", "11:30 AM", "Awaiting driver loading bay arrival")
                        )
                    ),
                    timestamp = now - 1000 * 60 * 10
                ),
                Shipment(
                    id = "CF-100",
                    clientName = "Prestige Metro Builders",
                    clientType = "Organization",
                    goods = "PVC Doors",
                    bay = "Bay No. 5",
                    quantity = 30,
                    destination = "Tower 4 Central Heights, Ring Road",
                    driver = "Ajax",
                    vehicle = "Mahindra Jeeto",
                    status = "Delivered",
                    createdAt = "08:45 AM",
                    deliveredAt = "10:10 AM",
                    deliveryNote = "Received in good condition with sign-off stamp",
                    timelineJson = Shipment.buildTimelineJson(
                        listOf(
                            TimelineEvent("Assigned", "08:45 AM", "Dispatched"),
                            TimelineEvent("At Loading Bay", "09:00 AM", "At Bay No. 5"),
                            TimelineEvent("In Transit", "09:20 AM", "En route via Expressway"),
                            TimelineEvent("Delivered", "10:10 AM", "Delivered to Site Supervisor")
                        )
                    ),
                    timestamp = now - 1000 * 60 * 90
                )
            )
            shipmentDao.insertAll(demoShipments)
        }

        val notifCount = notificationDao.getNotificationCount()
        if (notifCount == 0) {
            val now = System.currentTimeMillis()
            val demoNotifs = listOf(
                NotificationItem(
                    text = "Sam transitioned CF-101 to \"In Transit\" from Bay No. 1",
                    time = "10:45 AM",
                    type = "transit",
                    timestamp = now - 1000 * 60 * 15
                ),
                NotificationItem(
                    text = "Ram reached Bay No. 2 for Glass Bullet Proof pickup",
                    time = "11:20 AM",
                    type = "bay",
                    timestamp = now - 1000 * 60 * 10
                ),
                NotificationItem(
                    text = "Ajax confirmed delivery for CF-100 (PVC Doors)",
                    time = "10:10 AM",
                    type = "delivered",
                    timestamp = now - 1000 * 60 * 45
                )
            )
            notificationDao.insertAll(demoNotifs)
        }
    }

    suspend fun createNewDispatch(
        clientName: String,
        clientType: String,
        goods: String,
        quantity: Int,
        destination: String,
        driver: String,
        vehicle: String
    ): Shipment {
        val count = shipmentDao.getShipmentCount()
        val newId = "CF-${100 + count + 1}"
        val bay = MasterData.getBayForGoods(goods)
        val timeStr = getCurrentTimeString()

        val timeline = listOf(
            TimelineEvent("Assigned", timeStr, "Order dispatched by Admin to $driver")
        )

        val newShipment = Shipment(
            id = newId,
            clientName = clientName,
            clientType = clientType,
            goods = goods,
            bay = bay,
            quantity = quantity,
            destination = destination,
            driver = driver,
            vehicle = vehicle,
            status = "Assigned",
            createdAt = timeStr,
            timelineJson = Shipment.buildTimelineJson(timeline),
            timestamp = System.currentTimeMillis()
        )

        shipmentDao.insertShipment(newShipment)

        // Add Notification
        notificationDao.insertNotification(
            NotificationItem(
                text = "New dispatch #$newId assigned to $driver ($vehicle) at $bay",
                time = timeStr,
                type = "dispatch"
            )
        )

        return newShipment
    }

    suspend fun updateShipmentStatus(shipmentId: String, newStatus: String) {
        val existing = shipmentDao.getShipmentById(shipmentId) ?: return
        val timeStr = getCurrentTimeString()

        val timeline = existing.parseTimeline().toMutableList()
        val note = when (newStatus) {
            "At Loading Bay" -> "${existing.driver} arrived at ${existing.bay}"
            "In Transit" -> "${existing.driver} loaded ${existing.goods} and began transit"
            else -> "Status updated to $newStatus"
        }
        timeline.add(TimelineEvent(newStatus, timeStr, note))

        val updated = existing.copy(
            status = newStatus,
            timelineJson = Shipment.buildTimelineJson(timeline)
        )
        shipmentDao.updateShipment(updated)

        // Log notification
        val notifType = if (newStatus == "In Transit") "transit" else "bay"
        val notifText = when (newStatus) {
            "At Loading Bay" -> "${existing.driver} has arrived at ${existing.bay} to load ${existing.goods} (#${existing.id})."
            "In Transit" -> "${existing.driver} has loaded ${existing.quantity} of ${existing.goods} and started transit to ${existing.destination} (#${existing.id})."
            else -> "Order #${existing.id} updated to $newStatus by ${existing.driver}"
        }
        notificationDao.insertNotification(
            NotificationItem(text = notifText, time = timeStr, type = notifType)
        )
    }

    suspend fun confirmDelivery(shipmentId: String, deliveryNote: String) {
        val existing = shipmentDao.getShipmentById(shipmentId) ?: return
        val timeStr = getCurrentTimeString()

        val timeline = existing.parseTimeline().toMutableList()
        val noteText = if (deliveryNote.isNotBlank()) "Note: \"$deliveryNote\"" else "Signed off and delivered"
        timeline.add(TimelineEvent("Delivered", timeStr, noteText))

        val updated = existing.copy(
            status = "Delivered",
            deliveredAt = timeStr,
            deliveryNote = deliveryNote.ifBlank { "Signed off by client" },
            timelineJson = Shipment.buildTimelineJson(timeline)
        )
        shipmentDao.updateShipment(updated)

        // Notify Admin of successful delivery
        notificationDao.insertNotification(
            NotificationItem(
                text = "Order #${existing.id} successfully delivered to ${existing.clientName} by ${existing.driver}!",
                time = timeStr,
                type = "delivered"
            )
        )
    }

    suspend fun clearNotifications() {
        notificationDao.clearAll()
    }
}
