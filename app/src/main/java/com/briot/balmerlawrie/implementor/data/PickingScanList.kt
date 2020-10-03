package com.briot.balmerlawrie.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picking_scan_item_list")
public data class PickingScanList (
        @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "userId") val userId: String?,
        @ColumnInfo(name = "picklistName") val picklistName: String?,
        @ColumnInfo(name = "picklistID") val picklistID: String?,
        @ColumnInfo(name = "isViolated") val isViolated: Boolean?,
        @ColumnInfo(name = "partNumber") val partNumber: String?,
        @ColumnInfo(name = "purchaseOrderNumber") val purchaseOrderNumber: String?,
        @ColumnInfo(name = "quantity") val quantity: Int?,
        @ColumnInfo(name = "serialNumber") val serialNumber: String?,
        @ColumnInfo(name = "violatedSerialNumber") val violatedSerialNumber: String?,
        @ColumnInfo(name = "postItemId") val postItemId: Int?,
        @ColumnInfo(name = "timestamp") val timeStamp: Long

)