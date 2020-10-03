package com.briot.balmerlawrie.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInwards

@Dao
interface PickingScanListDao {

    @Query("SELECT * from picking_scan_item_list" )
    fun getAllItems(): List<MaterialInwards?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PickingScanList)

    @Query("SELECT * FROM picking_scan_item_list WHERE picklistName = :picklistName")
    fun getScanWithPickNumber(picklistName: String): List<PickingScanList>

    @Query("DELETE FROM picking_scan_item_list WHERE picklistName = :picklistName")
    fun deleteWithPickNumber(picklistName: String?)

    @Query("DELETE from picking_scan_item_list WHERE picklistName = :barcodeSerial")
    suspend fun deleteSelectedPutawayFromDB(barcodeSerial: String)

    @Query("DELETE from picking_scan_item_list")
    suspend fun deleteAll()
}