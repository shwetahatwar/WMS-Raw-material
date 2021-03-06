package com.briot.wms.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_item_list")
public data class DBAuditItem(

    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "serialNumber") val serialNumber: String?,
    @ColumnInfo(name = "batchCode") val batchCode: String?,
    @ColumnInfo(name = "productCode") val productCode: String?,
    @ColumnInfo(name = "projectId") val projectId: Int  ?


//    @ColumnInfo(name = "materialGenericName") val materialGenericName: String?,
//    @ColumnInfo(name = "materialDescription") val materialDescription: String?,
//    @ColumnInfo(name = "timestamp") val timeStamp: Long,
//    @ColumnInfo(name = "dispatchSlipId") val dispatchSlipId: Int,
//    @ColumnInfo(name = "dipatchSlipNumber") val dipatchSlipNumber: String?,
//    @ColumnInfo(name = "vehicleNumber") val vehicleNumber: String?,
//    @ColumnInfo(name = "submitted") val submitted: Int,
//    @ColumnInfo(name = "user") val user: String?,
//    @ColumnInfo(name = "submittedOn") val submittedOn: Long
)
