package com.briot.balmerlawrie.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.balmerlawrie.implementor.repository.remote.QCScanItem

@Dao
interface QCPendingScanListItemDao {

    @Query("SELECT * from qc_pending_scan_list")
    fun getAllItems(): List<QCScanItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: QCScanItemList)

    @Query("DELETE from qc_pending_scan_list WHERE barcodeSerial = :barcodeSerial")
    suspend fun deleteSelectedQCPendingFromDB(barcodeSerial: String)

    @Query("DELETE from qc_pending_scan_list")
    suspend fun deleteAll()
}