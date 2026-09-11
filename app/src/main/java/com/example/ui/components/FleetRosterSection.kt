package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FleetMember
import com.example.data.models.MasterData
import com.example.data.models.Shipment
import com.example.ui.theme.*

@Composable
fun FleetRosterSection(
    allShipments: List<Shipment>,
    onSimulateDriver: (String) -> Unit
) {
    val busyDrivers = allShipments
        .filter { it.status != "Delivered" }
        .map { it.driver }
        .toSet()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Slate200, RoundedCornerShape(18.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FLEET MASTER ROSTER & AVAILABILITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "5 Vehicles configured",
                    fontSize = 10.sp,
                    color = Slate400
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MasterData.FLEET_ROSTER.forEach { fleet ->
                    val isBusy = busyDrivers.contains(fleet.driver)
                    FleetMemberCard(
                        fleet = fleet,
                        isBusy = isBusy,
                        onSimulate = { onSimulateDriver(fleet.driver) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FleetMemberCard(
    fleet: FleetMember,
    isBusy: Boolean,
    onSimulate: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(145.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isBusy) Amber200 else Emerald200,
                RoundedCornerShape(14.dp)
            ),
        color = if (isBusy) Amber50.copy(alpha = 0.4f) else Emerald50.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fleet.driver,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isBusy) Amber100 else Emerald100)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isBusy) "BUSY" else "AVAILABLE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isBusy) Amber800 else Emerald700
                    )
                }
            }

            Text(
                text = fleet.vehicle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Slate600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = fleet.plate,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate400
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Slate200, RoundedCornerShape(6.dp))
                    .clickable(onClick = onSimulate)
                    .padding(vertical = 4.dp)
                    .testTag("simulate_driver_${fleet.driver}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Simulate Driver →",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            }
        }
    }
}
