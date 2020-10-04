package com.briot.wms.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "return_from_production_scan_list")
public data class ReturnFromProductionItemList(
        @PrimaryKey(autoGenerate = true) var id:Int?,
        @ColumnInfo(name = "materialInwardId") val materialInwardId: Int?,
        @ColumnInfo(name = "projectId") val projectId: Int?,
        @ColumnInfo(name = "quantity") val quantity: Int?,
        @ColumnInfo(name = "remarks") val remarks: String?,
        @ColumnInfo(name = "userId") val userId: Int?,
        @ColumnInfo(name = "barcodeSerial") val barcodeSerial: String?

)
