package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MasterData
import com.example.ui.theme.*

@Composable
fun FilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    driverFilter: String,
    onDriverFilterChange: (String) -> Unit,
    categoryFilter: String,
    onCategoryFilterChange: (String) -> Unit,
    cargoFilter: String,
    onCargoFilterChange: (String) -> Unit,
    onResetFilters: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Text Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shipments_search_input"),
                placeholder = {
                    Text(
                        text = "Search order ID, client, location, cargo...",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onSearchChange("")
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber500,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = Slate50,
                    unfocusedContainerColor = Slate50
                )
            )

            // Horizontal Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver Filter Dropdown
                FilterDropdownChip(
                    label = if (driverFilter.isEmpty()) "All Drivers" else driverFilter,
                    isSelected = driverFilter.isNotEmpty(),
                    options = listOf("All Drivers") + MasterData.FLEET_ROSTER.map { it.driver },
                    onOptionSelected = {
                        onDriverFilterChange(if (it == "All Drivers") "" else it)
                    }
                )

                // Category Filter Dropdown
                FilterDropdownChip(
                    label = if (categoryFilter.isEmpty()) "All Categories" else categoryFilter,
                    isSelected = categoryFilter.isNotEmpty(),
                    options = listOf("All Categories") + MasterData.CLIENT_CATEGORIES,
                    onOptionSelected = {
                        onCategoryFilterChange(if (it == "All Categories") "" else it)
                    }
                )

                // Cargo Filter Dropdown
                FilterDropdownChip(
                    label = if (cargoFilter.isEmpty()) "All Cargo" else cargoFilter,
                    isSelected = cargoFilter.isNotEmpty(),
                    options = listOf("All Cargo") + MasterData.GOODS_LIST.map { it.goods },
                    onOptionSelected = {
                        onCargoFilterChange(if (it == "All Cargo") "" else it)
                    }
                )

                // Reset Button
                val hasActiveFilters = searchQuery.isNotEmpty() || driverFilter.isNotEmpty() || categoryFilter.isNotEmpty() || cargoFilter.isNotEmpty()
                if (hasActiveFilters) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Amber50)
                            .border(1.dp, Amber200, RoundedCornerShape(10.dp))
                            .clickable(onClick = onResetFilters)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("reset_filters_button")
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber700
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownChip(
    label: String,
    isSelected: Boolean,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 1.dp,
                    color = if (isSelected) Amber500 else Slate200,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { expanded = true },
            color = if (isSelected) Amber50 else Slate50
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Amber800 else Slate700
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = if (isSelected) Amber800 else Slate500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (label == option) FontWeight.Bold else FontWeight.Normal,
                            color = if (label == option) Amber700 else Slate800
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}
