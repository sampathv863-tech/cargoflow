package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CargoMetrics
import com.example.ui.theme.*

@Composable
fun KpiCardsSection(metrics: CargoMetrics) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Active Shipments",
                value = metrics.activeCount.toString(),
                label = "in-pipeline",
                icon = Icons.Default.Inventory2,
                iconBg = Amber50,
                iconColor = Amber600,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "En Route",
                value = metrics.transitCount.toString(),
                label = "on road",
                icon = Icons.Default.ElectricBolt,
                iconBg = Blue50,
                iconColor = Blue600,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Completed Today",
                value = metrics.deliveredCount.toString(),
                label = "signed off",
                icon = Icons.Default.CheckCircle,
                iconBg = Emerald50,
                iconColor = Emerald600,
                valueColor = Emerald600,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Available Fleet",
                value = "${metrics.availableFleetCount}/${metrics.totalFleetCount}",
                label = "ready",
                icon = Icons.Default.LocalShipping,
                iconBg = Color(0xFFEEF2FF),
                iconColor = Color(0xFF6366F1),
                valueColor = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    valueColor: Color = Slate900,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate500
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = iconColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
