package com.briot.wms.implementor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IssueToProductionDao {

//    @Query("SELECT * from issuetoprod_scan_item_list" )
//    fun getAllItems(): List<MaterialInwards?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: IssueToProduction)

    @Query("SELECT * FROM issuetoprod_scan_item_list WHERE picklistName = :picklistName AND employeeId = :employeeId")
    fun getScanedItemsForUser(employeeId: String?, picklistName: String?): List<IssueToProduction>

    @Query("SELECT * FROM issuetoprod_scan_item_list")
    fun getAll(): List<IssueToProduction>


//    @Query("DELETE FROM issuetoprod_scan_item_list WHERE picklistName = :picklistName AND employeeId = :employeeId")
//    fun deleteScannedItem(picklistName: String?, employeeId: String?)

    @Query("DELETE FROM issuetoprod_scan_item_list WHERE picklistName = :picklistName AND employeeId = :employeeId")
    fun deleteScannedItem(picklistName: String?, employeeId: String?)

    @Query("UPDATE issuetoprod_scan_item_list SET employeeId = :employeeId where picklistName = :picklistName")
    fun updateValue(picklistName: String?, employeeId: String?)


    @Query("DELETE FROM issuetoprod_scan_item_list WHERE picklistName = :picklistName AND employeeId = :employeeId AND inputMaterialBarcode = :inputMaterialBarcode")
    fun deleteScannedItemWithBarcode(employeeId: String?, picklistName: String?, inputMaterialBarcode: String?)

    @Query("DELETE from issuetoprod_scan_item_list WHERE picklistName = :barcodeSerial")
    suspend fun deleteSelectedPutawayFromDB(barcodeSerial: String)

    @Query("DELETE from issuetoprod_scan_item_list")
    suspend fun deleteAll()

    @Query("UPDATE issuetoprod_scan_item_list SET quantity = :quantity WHERE picklistName = :picklistName AND employeeId = :employeeId AND inputMaterialBarcode = :serialNumber")
    fun updateQuantityScanWithPickNumber(serialNumber: String?, picklistName: String?, quantity: Int?, employeeId: String?)
}