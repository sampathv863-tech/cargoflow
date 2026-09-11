package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MasterData
import com.example.ui.theme.*

@Composable
fun TopNavBar(
    currentRole: String,
    notificationCount: Int,
    onRoleSelected: (String) -> Unit,
    onNotificationClick: () -> Unit
) {
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(Amber500, Amber600, Amber700))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "CargoFlow Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CargoFlow",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            letterSpacing = (-0.3).sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Amber100)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "TMS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Amber800
                            )
                        }
                    }
                    Text(
                        text = "Smart Dispatch & Fleet Logistics",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                }
            }

            // Role Switcher & Notification Bell
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role selector pill
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                            .clickable { roleDropdownExpanded = true }
                            .testTag("role_switcher_button"),
                        color = Slate100
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Role Icon",
                                tint = Amber600,
                                modifier = Modifier.size(14.dp)
                            )
                            val displayRole = if (currentRole == "admin") {
                                "Admin Dispatcher"
                            } else {
                                val fleet = MasterData.FLEET_ROSTER.find { it.driver == currentRole }
                                val vehicleShort = fleet?.vehicle?.split(" ")?.firstOrNull() ?: ""
                                "Driver: $currentRole ($vehicleShort)"
                            }
                            Text(
                                text = displayRole,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open Role Menu",
                                tint = Slate600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🏢 Admin Dispatcher",
                                    fontWeight = if (currentRole == "admin") FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentRole == "admin") Amber700 else Slate800
                                )
                            },
                            onClick = {
                                roleDropdownExpanded = false
                                onRoleSelected("admin")
                            }
                        )
                        HorizontalDivider()
                        MasterData.FLEET_ROSTER.forEach { fleet ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "🚚 Driver: ${fleet.driver} (${fleet.vehicle})",
                                        fontWeight = if (currentRole == fleet.driver) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentRole == fleet.driver) Amber700 else Slate800
                                    )
                                },
                                onClick = {
                                    roleDropdownExpanded = false
                                    onRoleSelected(fleet.driver)
                                }
                            )
                        }
                    }
                }

                // Notification Bell
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .clickable(onClick = onNotificationClick)
                        .testTag("notification_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Slate700,
                        modifier = Modifier.size(19.dp)
                    )
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(Amber600)
                                .border(1.5.dp, Color.White, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
