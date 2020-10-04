package com.briot.wms.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.briot.wms.implementor.repository.remote.auditProjectList

@Dao
interface DBAuditItemDao {

    @Query("SELECT * from audit_item_list WHERE projectId = :projectId ORDER BY serialNumber ASC")
    fun getAllItems(projectId: Int): List<auditProjectList>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DBAuditItem)

    @Query("DELETE from audit_item_list WHERE serialNumber = :serialNumber")
    suspend fun deleteSelectedAuditFromDB(serialNumber: String)

    @Query("DELETE from audit_item_list")
    suspend fun deleteAll()
}