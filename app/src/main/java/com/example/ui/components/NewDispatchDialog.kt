package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.FleetMember
import com.example.data.models.MasterData
import com.example.data.models.Shipment
import com.example.ui.theme.*

@Composable
fun NewDispatchDialog(
    allShipments: List<Shipment>,
    onDismiss: () -> Unit,
    onSubmit: (clientName: String, clientType: String, goods: String, quantity: Int, destination: String, driver: String, vehicle: String) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf(MasterData.CLIENT_CATEGORIES[0]) }
    var goods by remember { mutableStateOf(MasterData.GOODS_LIST[0].goods) }
    var quantityText by remember { mutableStateOf("25") }
    var destination by remember { mutableStateOf("") }

    // Check available fleet
    val busyDrivers = remember(allShipments) {
        allShipments.filter { it.status != "Delivered" }.map { it.driver }.toSet()
    }
    val availableFleet = remember(busyDrivers) {
        MasterData.FLEET_ROSTER.filter { !busyDrivers.contains(it.driver) }
    }

    var selectedFleetMember by remember(availableFleet) {
        mutableStateOf(availableFleet.firstOrNull())
    }

    var clientTypeExpanded by remember { mutableStateOf(false) }
    var goodsExpanded by remember { mutableStateOf(false) }
    var vehicleExpanded by remember { mutableStateOf(false) }

    val assignedBay = MasterData.getBayForGoods(goods)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Automatically hide keyboard and clear focus whenever the user scrolls
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Dialog(
        onDismissRequest = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.86f)
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Amber100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Order",
                                    tint = Amber700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Create New Dispatch Order",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Assign cargo, designated bay, and vehicle",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onDismiss()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Slate500
                            )
                        }
                    }

                    HorizontalDivider(color = Slate100)

                    // Client Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Client / Consignee Name *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dispatch_client_name_input"),
                            placeholder = { Text("e.g. Apex Glass Tower Ltd", fontSize = 12.sp, color = Slate400) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber500,
                                unfocusedBorderColor = Slate300
                            )
                        )
                    }

                    // Client Category Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Client Category *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Slate300, RoundedCornerShape(12.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        clientTypeExpanded = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = clientType,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Category",
                                        tint = Slate500
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = clientTypeExpanded,
                                onDismissRequest = { clientTypeExpanded = false }
                            ) {
                                MasterData.CLIENT_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 12.sp) },
                                        onClick = {
                                            clientType = cat
                                            clientTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Goods Item & Auto-assigned Loading Bay
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Goods Item & Assigned Loading Bay *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Goods selector
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Slate300, RoundedCornerShape(12.dp))
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        goodsExpanded = true
                                    }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = goods,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Slate800
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Goods",
                                            tint = Slate500
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = goodsExpanded,
                                    onDismissRequest = { goodsExpanded = false }
                                ) {
                                    MasterData.GOODS_LIST.forEach { item ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(item.goods, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("(${item.bay})", fontSize = 11.sp, color = Amber700)
                                                }
                                            },
                                            onClick = {
                                                goods = item.goods
                                                goodsExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Auto-assigned Bay Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Amber50)
                                    .border(1.dp, Amber300, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Amber600)
                                    )
                                    Text(
                                        text = assignedBay,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Amber800
                                    )
                                }
                            }
                        }
                    }

                    // Quantity and Destination
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.width(90.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Quantity *",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber500,
                                    unfocusedBorderColor = Slate300
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Destination Address *",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            OutlinedTextField(
                                value = destination,
                                onValueChange = { destination = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dispatch_destination_input"),
                                placeholder = { Text("e.g. Sector 62 Industrial Area", fontSize = 12.sp, color = Slate400) },
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
                                    focusedBorderColor = Amber500,
                                    unfocusedBorderColor = Slate300
                                )
                            )
                        }
                    }

                    // Assign Available Vehicle & Driver
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Assign Available Vehicle & Driver *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )

                        if (availableFleet.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Rose50)
                                    .border(1.dp, Rose100, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Rose600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "All vehicles currently have active dispatches. Free up a driver or wait for delivery.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Rose600
                                    )
                                }
                            }
                        } else {
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Slate300, RoundedCornerShape(12.dp))
                                        .clickable {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            vehicleExpanded = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedFleetMember?.let { "${it.vehicle} — Driver: ${it.driver} (${it.plate})" }
                                                ?: "Select available vehicle",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Slate800
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Vehicle",
                                            tint = Slate500
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = vehicleExpanded,
                                    onDismissRequest = { vehicleExpanded = false }
                                ) {
                                    availableFleet.forEach { fleet ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = "${fleet.vehicle} — Driver: ${fleet.driver}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Plate: ${fleet.plate}",
                                                        fontSize = 10.sp,
                                                        color = Slate500
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedFleetMember = fleet
                                                vehicleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modal Footer Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate600)
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSubmit = clientName.isNotBlank() &&
                                destination.isNotBlank() &&
                                quantityText.toIntOrNull() != null &&
                                selectedFleetMember != null

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                val qty = quantityText.toIntOrNull() ?: 1
                                val fleet = selectedFleetMember ?: return@Button
                                onSubmit(
                                    clientName,
                                    clientType,
                                    goods,
                                    qty,
                                    destination,
                                    fleet.driver,
                                    fleet.vehicle
                                )
                            },
                            enabled = canSubmit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Amber600,
                                disabledContainerColor = Slate300
                            ),
                            modifier = Modifier.testTag("submit_dispatch_button")
                        ) {
                            Text(
                                text = "Dispatch & Notify Driver",
                                fontSize = 12.sp,
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
