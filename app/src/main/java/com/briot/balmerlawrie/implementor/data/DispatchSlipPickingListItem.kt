package com.briot.balmerlawrie.implementor.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispatchslip_picking_list_item")
public data class DispatchSlipPickingListItem(

    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "serialNumber") val serialNumber: String?,
    @ColumnInfo(name = "batchCode") val batchCode: String?,
    @ColumnInfo(name = "productCode") val productCode: String?
)
