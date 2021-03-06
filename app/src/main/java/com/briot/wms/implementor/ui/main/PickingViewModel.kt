package com.briot.wms.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.data.AppDatabase
import com.briot.wms.implementor.data.PickingScanList
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.local.PrefRepository
import com.briot.wms.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.*
import kotlin.collections.ArrayList

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
    var diffpicklistMasterDisplayList = ArrayList<PickingMasterDisplay>()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessfulUpdate: LiveData<Boolean> = MutableLiveData()

    var barcodeSerial: String? = null
    var partNumber: String? = null

    var orgQuantity: Int? = null
    var remainingQuantity: Int? = null

    var batchNumber: String? = null
    var batchNoToUpdate: String? = null
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
        if (scanedItems.size > 0){
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

    suspend fun updateQuantityScanWithPickNumber(serialNumber: String?,
                                                 pickListNumber: String?, quantity: Int?){
        var dbDao = appDatabase.pickingScanListDao()
        dbDao.updateQuantityScanWithPickNumber(serialNumber, pickListNumber, quantity)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getScanWithPickNumber(pickListNumber)
                (itemSubmissionSuccessfulUpdate as MutableLiveData<Boolean>).value = true
            }
        }
    }

    suspend fun addItemInDatabase(itemListToAdd: PickingMasterDisplay, pickListNumber: String?,
                                          pickListId: String?, isViolated: Boolean?,
                                          quantity: Int?, totalQuantity: Int?) {
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
                                    postItemId= itemListToAdd.id, timeStamp = Date().time,
                                    totalQuantity =totalQuantity )

        println("dbItem-->"+dbItem)
        var dbDao = appDatabase.pickingScanListDao()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getScanWithPickNumber(pickListNumber)
            }
        }

//        if (isViolated!!){
//            for (i in scanedItems){
//                for (j in picklistMasterDisplayList){
//                    if (((i.serialNumber != j.batchNumber )) && !diffpicklistMasterDisplayList.contains(j)) {
//                        diffpicklistMasterDisplayList.add(j)
//                    }
//                }
//            }
//            for (i in diffpicklistMasterDisplayList){
//                println("diff item --"+i.barcodeSerial + "--batchnumber-> "+i.batchNumber)
//            }
//            val scannedItemFound = diffpicklistMasterDisplayList?.filter{
//                it!!.partNumber.toString() == partNumber.toString() }
//
//            if (scannedItemFound.size > 0 ){
//                picklistMasterDisplayList.remove(scannedItemFound[0])
//                scannedItemFound[0].numberOfPacks = quantity
//                picklistMasterDisplayList.add(0, scannedItemFound[0])
//                // diffpicklistMasterDisplayList = ArrayList<PickingMasterDisplay>()
//            }
//        }
    }

    fun getScanWithPickNumber(picklistName: String?) {
        var dbDao = appDatabase.pickingScanListDao()
        var dbItems = picklistName?.let { dbDao.getScanWithPickNumber(it) }
        if (dbItems != null) {
            scanedItems = dbItems
        }
        println("dbItems->"+dbItems?.size)
        for (i in scanedItems){
            println("db scanned quantity item -->"+i.quantity)
            println("db total quantity item -->"+i.totalQuantity)
        }
        var thisobj = this
        (thisobj.totalScannedItem as MutableLiveData<Number>).value = dbItems?.size

        // (thisobj.scanedItemList as MutableLiveData<List<PickingScanList?>>).value = dbItems

        // return dbItems
    }

    fun deleteWithPickName(picklistName: String?){
        var dbDao = appDatabase.pickingScanListDao()
        var dbItems = dbDao.deleteWithPickNumber(picklistName)
        // picklistMasterDisplayList = ArrayList<PickingMasterDisplay>()
        //loadPendingInProgCompltedList()
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
//        if (totalScannedItem.value.toString() == picklistMasterDisplay.value?.size.toString()){
//            itemToUpdate.completedPicklistId = id
//        }else{
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
                materialItemToUpdate += itemToAdd
                materialItemToUpdate.sortByDescending { it?.timestamp}
            }
            itemToUpdate.materials = materialItemToUpdate
            itemToUpdate.picklistId = id
            itemToUpdate.userId = userId.toInt()
        // }

        println("itemToUpdate submit-->"+itemToUpdate)

        RemoteRepository.singleInstance.postPickingMasterData( id.toString(), itemToUpdate,
                                                        this::handlePickingMasterResponse,
                                                        this::handlePickingMasterError)
    }

    private fun handlePickingMasterResponse(res: DispatchSlipItemResponse?) {
        var thisobj = this

        deleteWithPickName(picklistName)

        // After success navigate to previous screen
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }

    private fun handlePickingMasterError(error: Throwable) {
        Log.d(ContentValues.TAG,"error->"+error.localizedMessage)
    }
}
