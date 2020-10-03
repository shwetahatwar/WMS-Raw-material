package com.briot.balmerlawrie.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInwards

@Dao
interface PutawayScanListDao {

    @Query("SELECT * from putaway_scan_item_list" )
    fun getAllItems(): Array<MaterialInwards?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PutawayScanList)

    @Query("DELETE from putaway_scan_item_list WHERE barcodeSerial = :barcodeSerial")
    suspend fun deleteSelectedPutawayFromDB(barcodeSerial: String)

    @Query("DELETE from putaway_scan_item_list")
    suspend fun deleteAll()
}