package com.briot.balmerlawrie.implementor.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DispatchSlipPickingListItemDao {

    @Query("SELECT * from dispatchslip_picking_list_item ORDER BY serialNumber ASC")
    fun getAllItems(): LiveData<List<DispatchSlipPickingListItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DispatchSlipPickingListItem)

    @Query("DELETE from dispatchslip_picking_list_item")
    suspend fun deleteAll()
}