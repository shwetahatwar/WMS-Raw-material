package com.briot.wms.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issuetoprod_scan_item_list")
public data class IssueToProduction (
        @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "userId") val userId: Int?,
        @ColumnInfo(name = "inputMaterialBarcode") val inputMaterialBarcode: String?,
        @ColumnInfo(name = "projectId") val projectId: Int?,
        @ColumnInfo(name = "picklistId") val picklistId: Int?,
        @ColumnInfo(name = "picklistName") val picklistName: String?,
        @ColumnInfo(name = "quantity") val quantity: Int?,
        @ColumnInfo(name = "materialInwardId") val materialInwardId: Int?,
        @ColumnInfo(name = "employeeId") val employeeId: String?


)