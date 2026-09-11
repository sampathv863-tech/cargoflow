package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.Shipment
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY timestamp DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE id = :id LIMIT 1")
    suspend fun getShipmentById(id: String): Shipment?

    @Query("SELECT COUNT(*) FROM shipments")
    suspend fun getShipmentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shipments: List<Shipment>)

    @Update
    suspend fun updateShipment(shipment: Shipment)

    @Query("DELETE FROM shipments WHERE id = :id")
    suspend fun deleteShipmentById(id: String)
}
