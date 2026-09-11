package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.MasterData
import com.example.data.models.NotificationItem
import com.example.data.models.Shipment
import com.example.data.repository.CargoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CargoMetrics(
    val activeCount: Int = 0,
    val transitCount: Int = 0,
    val deliveredCount: Int = 0,
    val availableFleetCount: Int = 5,
    val totalFleetCount: Int = 5
)

class CargoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CargoRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = CargoRepository(db)
        viewModelScope.launch {
            repository.initializePreloadedDataIfNeeded()
        }
    }

    val shipments: StateFlow<List<Shipment>> = repository.allShipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Role state: "admin" or driver name ("Sam", "Ram", etc.)
    private val _currentRole = MutableStateFlow("admin")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Admin filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _driverFilter = MutableStateFlow("")
    val driverFilter: StateFlow<String> = _driverFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow("")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _cargoFilter = MutableStateFlow("")
    val cargoFilter: StateFlow<String> = _cargoFilter.asStateFlow()

    private val _adminStageTab = MutableStateFlow("all")
    val adminStageTab: StateFlow<String> = _adminStageTab.asStateFlow()

    // Driver tab: "active" or "history"
    private val _driverSubTab = MutableStateFlow("active")
    val driverSubTab: StateFlow<String> = _driverSubTab.asStateFlow()

    // Dialog & Sheet states
    private val _showNewDispatchDialog = MutableStateFlow(false)
    val showNewDispatchDialog: StateFlow<Boolean> = _showNewDispatchDialog.asStateFlow()

    private val _showNotificationSheet = MutableStateFlow(false)
    val showNotificationSheet: StateFlow<Boolean> = _showNotificationSheet.asStateFlow()

    private val _showCsvDialog = MutableStateFlow(false)
    val showCsvDialog: StateFlow<Boolean> = _showCsvDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private data class FilterParams(
        val query: String,
        val driver: String,
        val category: String,
        val cargo: String,
        val stage: String
    )

    private val filterParams = combine(
        _searchQuery,
        _driverFilter,
        _categoryFilter,
        _cargoFilter,
        _adminStageTab
    ) { q, d, c, g, s ->
        FilterParams(q, d, c, g, s)
    }

    // Combined filtered shipments for Admin
    val filteredShipments: StateFlow<List<Shipment>> = combine(
        shipments,
        filterParams
    ) { list, params ->
        val q = params.query.trim().lowercase()
        list.filter { item ->
            if (params.stage != "all" && item.status != params.stage) return@filter false
            if (params.driver.isNotBlank() && !item.driver.equals(params.driver, ignoreCase = true)) return@filter false
            if (params.category.isNotBlank() && !item.clientType.equals(params.category, ignoreCase = true)) return@filter false
            if (params.cargo.isNotBlank() && !item.goods.equals(params.cargo, ignoreCase = true)) return@filter false
            if (q.isNotBlank()) {
                val match = item.id.lowercase().contains(q) ||
                        item.clientName.lowercase().contains(q) ||
                        item.goods.lowercase().contains(q) ||
                        item.destination.lowercase().contains(q) ||
                        item.driver.lowercase().contains(q) ||
                        item.vehicle.lowercase().contains(q) ||
                        item.bay.lowercase().contains(q)
                if (!match) return@filter false
            }
            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // KPI Metrics
    val metrics: StateFlow<CargoMetrics> = shipments.combine(_currentRole) { list, _ ->
        val active = list.count { it.status != "Delivered" }
        val transit = list.count { it.status == "In Transit" }
        val delivered = list.count { it.status == "Delivered" }
        val busyDrivers = list.filter { it.status != "Delivered" }.map { it.driver }.toSet()
        val available = MasterData.FLEET_ROSTER.count { !busyDrivers.contains(it.driver) }
        CargoMetrics(
            activeCount = active,
            transitCount = transit,
            deliveredCount = delivered,
            availableFleetCount = available,
            totalFleetCount = MasterData.FLEET_ROSTER.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CargoMetrics())

    fun switchRole(role: String) {
        _currentRole.value = role
        val label = if (role == "admin") "Admin Dispatcher" else "Driver $role"
        showToast("Switched view to $label")
    }

    fun setAdminStageTab(stage: String) {
        _adminStageTab.value = stage
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDriverFilter(driver: String) {
        _driverFilter.value = driver
    }

    fun setCategoryFilter(cat: String) {
        _categoryFilter.value = cat
    }

    fun setCargoFilter(cargo: String) {
        _cargoFilter.value = cargo
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _driverFilter.value = ""
        _categoryFilter.value = ""
        _cargoFilter.value = ""
        _adminStageTab.value = "all"
        showToast("Filters reset")
    }

    fun setDriverSubTab(tab: String) {
        _driverSubTab.value = tab
    }

    fun openNewDispatchDialog() {
        _showNewDispatchDialog.value = true
    }

    fun closeNewDispatchDialog() {
        _showNewDispatchDialog.value = false
    }

    fun openNotificationSheet() {
        _showNotificationSheet.value = true
    }

    fun closeNotificationSheet() {
        _showNotificationSheet.value = false
    }

    fun openCsvDialog() {
        _showCsvDialog.value = true
    }

    fun closeCsvDialog() {
        _showCsvDialog.value = false
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun createDispatchOrder(
        clientName: String,
        clientType: String,
        goods: String,
        quantity: Int,
        destination: String,
        driver: String,
        vehicle: String
    ) {
        viewModelScope.launch {
            val order = repository.createNewDispatch(
                clientName = clientName.trim(),
                clientType = clientType,
                goods = goods,
                quantity = quantity,
                destination = destination.trim(),
                driver = driver,
                vehicle = vehicle
            )
            closeNewDispatchDialog()
            showToast("Order #${order.id} assigned to $driver!")
        }
    }

    fun updateStatus(shipmentId: String, nextStatus: String) {
        viewModelScope.launch {
            repository.updateShipmentStatus(shipmentId, nextStatus)
            showToast("Updated to: $nextStatus")
        }
    }

    fun confirmDelivery(shipmentId: String, note: String) {
        viewModelScope.launch {
            repository.confirmDelivery(shipmentId, note.trim())
            showToast("Delivery confirmed! Vehicle is now available.")
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
            showToast("Notification logs cleared")
        }
    }

    fun generateCsvData(): String {
        val list = shipments.value
        val sb = StringBuilder()
        sb.append("Order ID,Client Name,Category,Goods,Loading Bay,Quantity,Destination,Driver,Vehicle,Status,Delivered At,Delivery Note\n")
        for (s in list) {
            sb.append("\"${s.id}\",\"${s.clientName}\",\"${s.clientType}\",\"${s.goods}\",\"${s.bay}\",${s.quantity},\"${s.destination}\",\"${s.driver}\",\"${s.vehicle}\",\"${s.status}\",\"${s.deliveredAt ?: ""}\",\"${s.deliveryNote ?: ""}\"\n")
        }
        return sb.toString()
    }
}
