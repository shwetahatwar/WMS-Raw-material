package com.briot.balmerlawrie.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.balmerlawrie.implementor.repository.remote.QCScanItem
import com.briot.balmerlawrie.implementor.repository.remote.ReturnFromProductionResponse
import com.briot.balmerlawrie.implementor.repository.remote.ReturnFromProductionScanItem

@Dao
interface ReturnFromProductionListItemDao {

    @Query("SELECT * from return_from_production_scan_list" )
    fun getAllItems(): List<ReturnFromProductionScanItem?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ReturnFromProductionItemList)

    @Query("DELETE from return_from_production_scan_list")
    suspend fun deleteSelectedReturnFromProductionFromDB()

    @Query("DELETE from return_from_production_scan_list")
    suspend fun deleteAll()
}