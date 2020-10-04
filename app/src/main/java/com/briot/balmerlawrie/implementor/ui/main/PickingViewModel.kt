package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.data.AppDatabase
import com.briot.balmerlawrie.implementor.data.DispatchSlipPickingListItem
import com.briot.balmerlawrie.implementor.data.PickingScanList
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import com.briot.balmerlawrie.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

class PickingViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val TAG = "AuditProjectsVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val picklistMasterDisplay: LiveData<Array<PickingMasterDisplay?>> = MutableLiveData()
    var scanedItemList: LiveData<Array<PickingScanList?>> = MutableLiveData()
    val invalidProjects: Array<PickingMasterDisplay?> = arrayOf(null)
    val violatedData: LiveData<Array<MaterialInward?>> = MutableLiveData()

    var scanedItems: List<PickingScanList> = emptyList()
    val totalScannedItem: LiveData<Number> = MutableLiveData()
    var picklistMasterCount: Number? = 0
    var picklistMasterDisplayList = ArrayList<PickingMasterDisplay>()
    var barcodeSerial: String? = null
    var batchNumber: String? = null
    var status: String? = null
    var id: Int? = null
    var postItemId: Int? = null
    var picklistName: String? = null
    var errorMessage: String = ""
    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())

    fun loadPendingInProgCompltedList(){
        RemoteRepository.singleInstance.getCompletedItems(id.toString(),
                this::handleloadPendingInProgCompltedResponse,
                this::handleloadPendingInProgCompltedError)
    }


    private fun handleloadPendingInProgCompltedResponse(picklistmaster: Array<PickingMasterDisplay?>) {
        // Log.d(TAG, "successful project list" + projects.toString())
        // picklistMasterCount = picklistmaster!!.total!!.toInt()
        picklistMasterCount = picklistmaster.size

        var thisobj = this
        for (i in picklistmaster){
            if (i != null) {
                picklistMasterDisplayList.add(i)
            }
        }

        // scanned item at top
        for (i in scanedItems){
            val scannedItemFound = picklistMasterDisplayList?.filter{
                it!!.batchNumber.toString() == i.serialNumber.toString() ||
                        it!!.batchNumber.toString() == i.violatedSerialNumber.toString()}
            if (scannedItemFound.size > 0){
                picklistMasterDisplayList.remove(scannedItemFound[0])
                scannedItemFound[0].numberOfPacks = i.quantity
                picklistMasterDisplayList.add(0, scannedItemFound[0])
            }
        }
        (this.picklistMasterDisplay as MutableLiveData<Array<PickingMasterDisplay?>>).value = picklistmaster
    }


    private fun handleloadPendingInProgCompltedError(error: Throwable) {
        Log.d(TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.picklistMasterDisplay as MutableLiveData<Array<PickingMasterDisplay?>>).value = invalidProjects
        }
    }


    suspend fun addItemInDatabase(itemListToAdd: PickingMasterDisplay, pickListNumber: String?,
                                          pickListId: String?, isViolated: Boolean?,
                                  quantity: Int?) {
        var userId: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().ROLE_ID, "")

        var dbItem = PickingScanList(id = null,
                                    userId = userId,
                                    picklistName = pickListNumber,
                                    picklistID = pickListId,
                                    isViolated = isViolated,
                                    partNumber = itemListToAdd.partNumber,
                                    purchaseOrderNumber = itemListToAdd.purchaseOrderNumber,
                                    quantity = quantity,
                                    serialNumber = itemListToAdd.batchNumber,
                                    violatedSerialNumber = barcodeSerial,
                                    postItemId= itemListToAdd.id, timeStamp = Date().time)

        println("dbItem-->"+dbItem)
        var dbDao = appDatabase.pickingScanListDao()
        dbDao.insert(item = dbItem)

//        if (isViolated!!){
//            val violatedItemToDisplay = PickingMasterDisplay()
//            violatedItemToDisplay.partNumber = pickListNumber
//            violatedItemToDisplay.batchNumber =  barcodeSerial
//            violatedItemToDisplay.partDescription = itemListToAdd.partDescription
//            violatedItemToDisplay.quantityPicked = quantity.toString()
//            violatedItemToDisplay.location = itemListToAdd.location
//            picklistMasterDisplayList.add(0, violatedItemToDisplay)
//        }
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getScanWithPickNumber(pickListNumber)
            }
        }
    }

    fun getScanWithPickNumber(picklistName: String?) {
        var dbDao = appDatabase.pickingScanListDao()
        var dbItems = picklistName?.let { dbDao.getScanWithPickNumber(it) }
        if (dbItems != null) {
            scanedItems = dbItems
        }
        println("dbItems->"+dbItems?.size)
        var thisobj = this
        (thisobj.totalScannedItem as MutableLiveData<Number>).value = dbItems?.size

        // (thisobj.scanedItemList as MutableLiveData<List<PickingScanList?>>).value = dbItems

        // return dbItems
    }

    fun deleteWithPickName(picklistName: String?){
        var dbDao = appDatabase.pickingScanListDao()
        var dbItems = dbDao.deleteWithPickNumber(picklistName)
        getScanWithPickNumber(picklistName)
    }



    // Load violated data
    fun loadPutawayMaterialScan(barcodeSerial: String){
        (networkError as MutableLiveData<Boolean>).value = false
        RemoteRepository.singleInstance.getPutawayMaterialScan(barcodeSerial,
                this::handlePutawayMaterialScanResponse,
                this::handlePutawayMaterialScanError)
    }

    private fun handlePutawayMaterialScanResponse(res: Array<MaterialInward?>) {
        var thisobj = this
        (thisobj.violatedData as MutableLiveData<Array<MaterialInward?>>).value = res


//        var thisobj = this
////        (thisobj.putawayMaterialScanData as MutableLiveData<Array<MaterialInward?>>).value = putawayMaterialScan
        // putMaterialScantems()
//        GlobalScope.launch {
//            withContext(Dispatchers.Main) {
//                addPutawayMaterial(res[0])
//            }
//        }
    }

    private fun handlePutawayMaterialScanError(error: Throwable) {
//        Log.d(ContentValues.TAG,"error->"+error.localizedMessage)
//        Log.d(ContentValues.TAG, "error msg--->"+error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            println("------inside network error----")
            errorMessage = error.message.toString()
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            errorMessage = error.message.toString()
//            (this.violatedData as MutableLiveData<Array<MaterialInward?>>).value = res
        }

    }


    fun submitPickingMaster() {
        getScanWithPickNumber(picklistName)
        var userId: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().ROLE_ID, "")
        var itemToUpdate = PostPickingData()
        var materialItemToUpdate: Array<Materials> =  emptyArray()

        // if all items are scanned then request body is different
        if (totalScannedItem.value.toString() == picklistMasterDisplay.value?.size.toString()){
            itemToUpdate.completedPicklistId = id
        }else{
            for (i in scanedItems){
                var itemToAdd =  Materials()
                itemToAdd.batchNumber = i.serialNumber
                itemToAdd.partNumber = i.partNumber
                itemToAdd.quantity = i.quantity
                itemToAdd.serialNumber = i.serialNumber
                if (i.isViolated == true){
                    itemToAdd.purchaseOrderNumber = i.purchaseOrderNumber
                    itemToAdd.isViolated = i.isViolated
                    itemToAdd.violatedSerialNumber = i.violatedSerialNumber
                }
                println("itemToAdd-->"+itemToAdd)
                materialItemToUpdate += itemToAdd
                materialItemToUpdate.sortByDescending { it?.timestamp}
            }
            itemToUpdate.materials = materialItemToUpdate
            itemToUpdate.picklistId = id
            itemToUpdate.userId = userId.toInt()
        }

        RemoteRepository.singleInstance.postPickingMasterData( id.toString(), itemToUpdate,
                                                        this::handlePickingMasterResponse,
                                                        this::handlePickingMasterError)
    }

    private fun handlePickingMasterResponse(res: DispatchSlipItemResponse?) {
        var thisobj = this

        deleteWithPickName(picklistName)
        // (thisobj.violatedData as MutableLiveData<Array<DispatchSlipItemResponse?>>).value = res

//        var thisobj = this
////        (thisobj.putawayMaterialScanData as MutableLiveData<Array<MaterialInward?>>).value = putawayMaterialScan
        // putMaterialScantems()
//        GlobalScope.launch {
//            withContext(Dispatchers.Main) {
//                addPutawayMaterial(res[0])
//            }
//        }
    }

    private fun handlePickingMasterError(error: Throwable) {
        Log.d(ContentValues.TAG,"error->"+error.localizedMessage)
    }
}
