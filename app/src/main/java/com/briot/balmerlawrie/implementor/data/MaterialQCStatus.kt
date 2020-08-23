package com.briot.balmerlawrie.implementor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "material_qc_status")
public data class MaterialQCStatusDB(

        @PrimaryKey(autoGenerate = true) var id : Int?,
        @ColumnInfo(name = "barcodeSerial") val barcodeSerial: String?,
        @ColumnInfo(name = "partnumber") val partnumber: String?,
        @ColumnInfo(name = "QCStatus") val QCStatus: String?,
        @ColumnInfo(name = "location") val location: String?,
        @ColumnInfo(name = "qcId") val qcId: Int?,
        @ColumnInfo(name = "description") val description: String?
        )
