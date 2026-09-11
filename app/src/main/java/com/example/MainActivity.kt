package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CargoViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: CargoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CargoFlowApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CargoFlowApp(viewModel: CargoViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allShipments by viewModel.shipments.collectAsStateWithLifecycle()
    val filteredShipments by viewModel.filteredShipments.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val driverFilter by viewModel.driverFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val cargoFilter by viewModel.cargoFilter.collectAsStateWithLifecycle()
    val adminStageTab by viewModel.adminStageTab.collectAsStateWithLifecycle()
    val driverSubTab by viewModel.driverSubTab.collectAsStateWithLifecycle()

    val showNewDispatchDialog by viewModel.showNewDispatchDialog.collectAsStateWithLifecycle()
    val showNotificationSheet by viewModel.showNotificationSheet.collectAsStateWithLifecycle()
    val showCsvDialog by viewModel.showCsvDialog.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Auto dismiss toast after 3 seconds
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val lazyListState = rememberLazyListState()
    val driverListState = rememberLazyListState()

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(driverListState.isScrollInProgress) {
        if (driverListState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar with Role Switcher & Notification Bell
            TopNavBar(
                currentRole = currentRole,
                notificationCount = notifications.size,
                onRoleSelected = { role -> viewModel.switchRole(role) },
                onNotificationClick = { viewModel.openNotificationSheet() }
            )

            // Content Area: Admin View or Driver Portal View
            if (currentRole == "admin") {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Admin Banner Header
                    item {
                        AdminHeaderBanner(
                            onExportCsv = { viewModel.openCsvDialog() },
                            onNewDispatch = { viewModel.openNewDispatchDialog() }
                        )
                    }

                    // 4 KPI Summary Cards
                    item {
                        KpiCardsSection(metrics = metrics)
                    }

                    // Search & Filters Section
                    item {
                        FilterSection(
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            driverFilter = driverFilter,
                            onDriverFilterChange = { viewModel.setDriverFilter(it) },
                            categoryFilter = categoryFilter,
                            onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                            cargoFilter = cargoFilter,
                            onCargoFilterChange = { viewModel.setCargoFilter(it) },
                            onResetFilters = { viewModel.resetFilters() }
                        )
                    }

                    // Stage Tabs (All Orders, Assigned, At Bay, In Transit, Delivered)
                    item {
                        StageTabsSection(
                            allShipments = allShipments,
                            selectedStage = adminStageTab,
                            onStageSelected = { viewModel.setAdminStageTab(it) }
                        )
                    }

                    // Shipments Grid / List
                    if (filteredShipments.isEmpty()) {
                        item {
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
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Slate100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📦", fontSize = 20.sp)
                                    }
                                    Text(
                                        text = "No shipments found",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                    Text(
                                        text = "Try altering your filters or create a new dispatch order.",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredShipments, key = { it.id }) { item ->
                            ShipmentCard(
                                shipment = item,
                                onViewAsDriver = { driver -> viewModel.switchRole(driver) }
                            )
                        }
                    }

                    // Fleet Master Roster & Availability (5 Vehicles)
                    item {
                        FleetRosterSection(
                            allShipments = allShipments,
                            onSimulateDriver = { driver -> viewModel.switchRole(driver) }
                        )
                    }

                    // Footer note
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CargoFlow Logistics Suite • Preset Master Fleet",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            } else {
                // Driver Portal View
                LazyColumn(
                    state = driverListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        DriverPortalView(
                            driverName = currentRole,
                            allShipments = allShipments,
                            driverSubTab = driverSubTab,
                            onSubTabChanged = { viewModel.setDriverSubTab(it) },
                            onUpdateStatus = { id, nextStatus ->
                                viewModel.updateStatus(id, nextStatus)
                            },
                            onConfirmDelivery = { id, note ->
                                viewModel.confirmDelivery(id, note)
                            },
                            onSwitchAdmin = {
                                viewModel.switchRole("admin")
                            }
                        )
                    }
                }
            }
        }

        // Animated Toast Banner
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                color = Slate900,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✓", color = Emerald500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = toastMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // New Dispatch Order Dialog
        if (showNewDispatchDialog) {
            NewDispatchDialog(
                allShipments = allShipments,
                onDismiss = { viewModel.closeNewDispatchDialog() },
                onSubmit = { clientName, clientType, goods, quantity, destination, driver, vehicle ->
                    viewModel.createDispatchOrder(
                        clientName = clientName,
                        clientType = clientType,
                        goods = goods,
                        quantity = quantity,
                        destination = destination,
                        driver = driver,
                        vehicle = vehicle
                    )
                }
            )
        }

        // Notification Drawer / BottomSheet
        if (showNotificationSheet) {
            NotificationSheet(
                notifications = notifications,
                onDismiss = { viewModel.closeNotificationSheet() },
                onClearAll = { viewModel.clearNotifications() }
            )
        }

        // CSV Export Dialog
        if (showCsvDialog) {
            CsvExportDialog(
                csvContent = viewModel.generateCsvData(),
                onDismiss = { viewModel.closeCsvDialog() },
                onCopied = {
                    viewModel.showToast("CSV copied to clipboard")
                }
            )
        }
    }
}

@Composable
private fun AdminHeaderBanner(
    onExportCsv: () -> Unit,
    onNewDispatch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Amber200.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Amber50, Amber100.copy(alpha = 0.2f), Color.White)
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Fleet Operations Hub",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate900,
                                letterSpacing = (-0.3).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald100)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Emerald600)
                                    )
                                    Text(
                                        text = "Live Fleet",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald800
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Manage bays, assign shipments, and monitor driver progress in real time.",
                            fontSize = 11.sp,
                            color = Slate500,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_export_csv_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Slate700
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export CSV",
                            tint = Slate600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export CSV",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onNewDispatch,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("admin_new_dispatch_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Dispatch",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Dispatch",
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

