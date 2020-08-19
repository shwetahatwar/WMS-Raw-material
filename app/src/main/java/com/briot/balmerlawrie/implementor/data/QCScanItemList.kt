package com.briot.balmerlawrie.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qc_pending_scan_list")
public data class QCScanItemList(

        @PrimaryKey(autoGenerate = true) var id:Int?,
        @ColumnInfo(name = "barcodeSerial") val barcodeSerial: String?,
        @ColumnInfo(name = "qcId") val qcId: Int?,
        @ColumnInfo(name = "QCStatus") val QCStatus: Int?,
        @ColumnInfo(name = "prevQCStatus") val prevQCStatus: Int?
)
