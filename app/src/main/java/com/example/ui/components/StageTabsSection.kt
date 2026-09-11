package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Shipment
import com.example.ui.theme.*

@Composable
fun StageTabsSection(
    allShipments: List<Shipment>,
    selectedStage: String,
    onStageSelected: (String) -> Unit
) {
    val countAll = allShipments.size
    val countAssigned = allShipments.count { it.status == "Assigned" }
    val countAtBay = allShipments.count { it.status == "At Loading Bay" }
    val countTransit = allShipments.count { it.status == "In Transit" }
    val countDelivered = allShipments.count { it.status == "Delivered" }

    val tabs = listOf(
        Triple("all", "All Orders", countAll),
        Triple("Assigned", "📋 Assigned", countAssigned),
        Triple("At Loading Bay", "🏗️ At Bay", countAtBay),
        Triple("In Transit", "🚚 In Transit", countTransit),
        Triple("Delivered", "✅ Delivered", countDelivered)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (key, label, count) ->
            val isActive = selectedStage == key

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) Amber50 else Color.Transparent)
                    .clickable { onStageSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("stage_tab_$key"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isActive) Amber700 else Slate500
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isActive) Amber200 else Slate200
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Amber900 else Slate600
                        )
                    }
                }
            }
        }
    }
}
