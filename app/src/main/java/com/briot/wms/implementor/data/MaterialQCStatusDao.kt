package com.briot.wms.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.wms.implementor.repository.remote.qcStatusDisplay

@Dao
interface MaterialQCStatusDao {

    @Query("SELECT * from material_qc_status" )
    fun getAllItems(): Array<qcStatusDisplay?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MaterialQCStatusDB)

    @Query("DELETE from material_qc_status WHERE barcodeSerial = :barcodeSerial")
    suspend fun deleteSelectedQCPendingFromDB(barcodeSerial: String)

    @Query("DELETE from material_qc_status")
    suspend fun deleteAll()
}