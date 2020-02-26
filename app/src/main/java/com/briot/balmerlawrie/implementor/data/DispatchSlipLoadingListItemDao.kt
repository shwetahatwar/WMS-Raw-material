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

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId ORDER BY timestamp ASC")
    fun getAllDispatchSlipItems(dispatchSlipId: Int): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("SELECT COUNT (*) FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND productCode LIKE :materialCode AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getCountForBatchMaterialCode(dispatchSlipId: Int, materialCode: String, batchNumber: String): Int

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getItemsForBatch(dispatchSlipId: Int, batchNumber: String): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("SELECT * FROM dispatchslip_loading_list_item WHERE dispatchSlipId LIKE :dispatchSlipId AND productCode LIKE :materialCode AND batchCode LIKE :batchNumber ORDER BY timestamp ASC")
    fun getItemsForBatchMaterialCode(dispatchSlipId: Int, materialCode: String, batchNumber: String): LiveData<List<DispatchSlipLoadingListItem>>

    @Query("DELETE from dispatchslip_loading_list_item")
    suspend fun deleteAll()
}