package com.briot.balmerlawrie.implementor.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DispatchSlipLoadingListItemDao {

    @Query("SELECT * from dispatchslip_loading_list_item ORDER BY serialNumber ASC")
    fun getAllItems(): LiveData<List<DispatchSlipLoadingListItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DispatchSlipLoadingListItem)

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId = :dispatchSlipId ORDER BY timestamp ASC")
    fun getAllDispatchSlipItems(dispatchSlipId: Int): List<DispatchSlipLoadingListItem>

    @Query("SELECT COUNt (*) FROM dispatchslip_loading_list_item WHERE dispatchSlipId = :dispatchSlipId ORDER BY timestamp ASC")
    fun getAllDispatchSlipItemsCount(dispatchSlipId: Int): Int

    @Query("SELECT COUNT (*) FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND productCode LIKE :materialCode AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getCountForBatchMaterialCode(dispatchSlipId: Int, materialCode: String, batchNumber: String): Int

    @Query("SELECT COUNT (*) FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND productCode LIKE :materialCode AND batchCode LIKE :batchNumber AND serialNumber LIKE :serialNumber ORDER BY timestamp ASC")
    fun getCountForBatchMaterialCodeSerial(dispatchSlipId: Int, materialCode: String, batchNumber: String, serialNumber: String): Int

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getItemsForBatch(dispatchSlipId: Int, batchNumber: String): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND productCode LIKE :materialCode AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getItemsForBatchMaterialCode(dispatchSlipId: Int, materialCode: String, batchNumber: String): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND submitted = 1 ORDER BY timestamp ASC")
    fun getSubmitedDispatchDetails(dispatchSlipId: Int): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("UPDATE dispatchslip_loading_list_item SET submitted = 1, submittedOn = :timestamp WHERE dispatchSlipId LIKE :dipsatchSlipId")
    suspend fun updateSubmittedStatus(dipsatchSlipId: String,  timestamp: Long)

    @Query("DELETE from dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dipsatchSlipId")
    suspend fun deleteForSelectedDispatchSlip(dipsatchSlipId: String)

    @Query("DELETE from dispatchslip_loading_list_item WHERE dispatchSlipId = :dipsatchSlipId AND serialNumber LIKE :serialNumber")
    suspend fun deleteForSelectedDispatchSlipSerialNumber(dipsatchSlipId: Int, serialNumber: String)


    @Query("DELETE from dispatchslip_loading_list_item")
    suspend fun deleteAll()
}