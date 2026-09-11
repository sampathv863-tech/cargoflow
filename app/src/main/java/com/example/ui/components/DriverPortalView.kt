package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MasterData
import com.example.data.models.Shipment
import com.example.ui.theme.*

@Composable
fun DriverPortalView(
    driverName: String,
    allShipments: List<Shipment>,
    driverSubTab: String,
    onSubTabChanged: (String) -> Unit,
    onUpdateStatus: (shipmentId: String, newStatus: String) -> Unit,
    onConfirmDelivery: (shipmentId: String, note: String) -> Unit,
    onSwitchAdmin: () -> Unit
) {
    val fleetInfo = MasterData.FLEET_ROSTER.find { it.driver == driverName }
        ?: MasterData.FLEET_ROSTER[0]

    val activeTrip = allShipments.find { it.driver == driverName && it.status != "Delivered" }
    val historyList = allShipments.filter { it.driver == driverName && it.status == "Delivered" }

    var deliveryNote by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Driver Profile Card (Dark slate gradient)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Slate900
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Slate900, Slate800, Color(0xFF172554))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(listOf(Amber400, Amber600))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = driverName.take(1),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate950
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Driver $driverName",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (activeTrip != null) Amber500.copy(alpha = 0.25f) else Emerald500.copy(alpha = 0.25f)
                                        )
                                        .border(
                                            1.dp,
                                            if (activeTrip != null) Amber500.copy(alpha = 0.5f) else Emerald500.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (activeTrip != null) "TRIP ACTIVE" else "AVAILABLE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (activeTrip != null) Amber300 else Emerald300
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = "Vehicle",
                                    tint = Amber400,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${fleetInfo.vehicle} (${fleetInfo.plate})",
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                            }
                        }
                    }

                    // Switch to Admin button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onSwitchAdmin)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("switch_to_admin_button")
                    ) {
                        Text(
                            text = "Admin Hub",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber300
                        )
                    }
                }
            }
        }

        // Subtabs: Active Trip vs Trip History
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            color = Slate200.copy(alpha = 0.8f)
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isActiveTab = driverSubTab == "active"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActiveTab) Color.White else Color.Transparent)
                        .clickable { onSubTabChanged("active") }
                        .padding(vertical = 8.dp)
                        .testTag("driver_tab_active"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚀 Active Trip",
                        fontSize = 12.sp,
                        fontWeight = if (isActiveTab) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isActiveTab) Slate900 else Slate600
                    )
                }

                val isHistoryTab = driverSubTab == "history"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isHistoryTab) Color.White else Color.Transparent)
                        .clickable { onSubTabChanged("history") }
                        .padding(vertical = 8.dp)
                        .testTag("driver_tab_history"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📜 Trip History (${historyList.size})",
                        fontSize = 12.sp,
                        fontWeight = if (isHistoryTab) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isHistoryTab) Slate900 else Slate600
                    )
                }
            }
        }

        // Active Trip Subview
        if (driverSubTab == "active") {
            if (activeTrip == null) {
                // Empty state
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(18.dp)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☕", fontSize = 28.sp)
                        }
                        Text(
                            text = "No Active Assignment",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "You're in the ready pool. Switch to Admin Hub to dispatch a new shipment for $driverName.",
                            fontSize = 11.sp,
                            color = Slate500,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onSwitchAdmin,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                        ) {
                            Text(
                                text = "Dispatch a Trip as Admin →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Active Assignment Card
                val stepNum = when (activeTrip.status) {
                    "Assigned" -> 1
                    "At Loading Bay" -> 2
                    else -> 3
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, Amber400, RoundedCornerShape(18.dp)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Amber50)
                                        .border(1.dp, Amber200, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CURRENT WORK ORDER",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Amber800
                                    )
                                }
                                Text(
                                    text = "#${activeTrip.id}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate900
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Status",
                                    fontSize = 10.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = activeTrip.status,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Amber600
                                )
                            }
                        }

                        // Stepper Progress Indicator
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "1. Dock at Bay",
                                    fontSize = 10.sp,
                                    fontWeight = if (stepNum >= 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (stepNum >= 1) Amber700 else Slate400
                                )
                                Text(
                                    text = "2. Load Cargo",
                                    fontSize = 10.sp,
                                    fontWeight = if (stepNum >= 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (stepNum >= 2) Amber700 else Slate400
                                )
                                Text(
                                    text = "3. Deliver",
                                    fontSize = 10.sp,
                                    fontWeight = if (stepNum >= 3) FontWeight.Bold else FontWeight.Normal,
                                    color = if (stepNum >= 3) Blue700 else Slate400
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Slate100)
                            ) {
                                val progressFraction = when (stepNum) {
                                    1 -> 0.33f
                                    2 -> 0.66f
                                    else -> 1.0f
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (stepNum == 3) Blue500 else Amber500
                                        )
                                )
                            }
                        }

                        // Prominent Designated Bay Banner
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            color = Amber500
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(listOf(Amber500, Amber600))
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "DESIGNATED PICKUP STATION",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Amber100
                                        )
                                        Text(
                                            text = "🏷️ ${activeTrip.bay}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Cargo: ${activeTrip.goods}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Amber50
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${activeTrip.quantity} Units",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Client & Address details
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Slate100, RoundedCornerShape(12.dp)),
                            color = Slate50
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Client:", fontSize = 11.sp, color = Slate500)
                                    Text(activeTrip.clientName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Category:", fontSize = 11.sp, color = Slate500)
                                    Text(activeTrip.clientType, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                }
                                HorizontalDivider(color = Slate200.copy(alpha = 0.5f))
                                Column {
                                    Text("Delivery Address:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                    Text("📍 ${activeTrip.destination}", fontSize = 11.sp, color = Slate800, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Step-by-Step Action Progression Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (activeTrip.status) {
                                "Assigned" -> {
                                    Button(
                                        onClick = {
                                            onUpdateStatus(activeTrip.id, "At Loading Bay")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("driver_reach_bay_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                                    ) {
                                        Text(
                                            text = "🏗️ Step 1: Arrived at ${activeTrip.bay}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                                "At Loading Bay" -> {
                                    Button(
                                        onClick = {
                                            onUpdateStatus(activeTrip.id, "In Transit")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("driver_start_transit_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                                    ) {
                                        Text(
                                            text = "🚚 Step 2: Cargo Loaded & Start Transit",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                                "In Transit" -> {
                                    OutlinedTextField(
                                        value = deliveryNote,
                                        onValueChange = { deliveryNote = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("driver_delivery_note_input"),
                                        placeholder = {
                                            Text(
                                                "Optional delivery note or recipient name...",
                                                fontSize = 11.sp,
                                                color = Slate400
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Emerald500,
                                            unfocusedBorderColor = Slate300
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            onConfirmDelivery(activeTrip.id, deliveryNote)
                                            deliveryNote = ""
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("driver_confirm_delivery_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                    ) {
                                        Text(
                                            text = "✅ Step 3: Confirm Delivery",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Trip Activity Trail
                        val timeline = activeTrip.parseTimeline()
                        if (timeline.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "TRIP ACTIVITY TRAIL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate400,
                                    letterSpacing = 0.5.sp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    timeline.forEach { ev ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Amber500)
                                            )
                                            Column {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = ev.status,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate800
                                                    )
                                                    Text(
                                                        text = "• ${ev.time}",
                                                        fontSize = 10.sp,
                                                        color = Slate400
                                                    )
                                                }
                                                if (ev.note.isNotBlank()) {
                                                    Text(
                                                        text = ev.note,
                                                        fontSize = 10.sp,
                                                        color = Slate500
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Trip History Subview
            if (historyList.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed deliveries recorded yet for $driverName.",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    historyList.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(14.dp)),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${item.id}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate900
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Emerald100)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Delivered",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald800
                                        )
                                    }
                                }
                                Text(
                                    text = "${item.clientName} (${item.goods})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Text(
                                    text = "📍 ${item.destination}",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                                if (!item.deliveryNote.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Slate50)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "\"${item.deliveryNote}\"",
                                            fontSize = 10.sp,
                                            color = Slate600
                                        )
                                    }
                                }
                                HorizontalDivider(color = Slate100)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Delivered: ${item.deliveredAt ?: "Earlier"}",
                                        fontSize = 9.sp,
                                        color = Slate400
                                    )
                                    Text(
                                        text = "${item.quantity} units",
                                        fontSize = 9.sp,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
