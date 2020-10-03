package com.briot.balmerlawrie.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "putaway_scan_item_list")
public data class PutawayScanList (
        @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "barcodeSerial") val barcodeSerial: String?,
        @ColumnInfo(name = "partnumber") val partnumber: String?,
        @ColumnInfo(name = "description") val description: String?,
        @ColumnInfo(name = "eachPackQuantity") val eachPackQuantity: Int?,
        @ColumnInfo(name = "timestamp") val timeStamp: Long
)
