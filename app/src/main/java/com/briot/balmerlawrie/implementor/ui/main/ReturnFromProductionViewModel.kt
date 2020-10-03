package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.data.AppDatabase
import com.briot.balmerlawrie.implementor.data.IssueToProduction
import com.briot.balmerlawrie.implementor.data.ReturnFromProductionItemList
import com.briot.balmerlawrie.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody

class ReturnFromProductionViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var employeeId: String? = ""
    var qcScannedCount: Number? = 0
    var barcodeSerial: String? = null
    var batchNumber: String? = null
    var partNumber: String? = null
    var remarks: String? = null
    var projectId: Int? = null
    var quantity: Int? = null
    var performedBy: Int? = null
    var inputMaterialBarcode: String? = null
    val totalScannedItem: LiveData<Number> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val networkError: LiveData<Boolean> = MutableLiveData()
    val returnfromproduction: LiveData<Array<ReturnFromProduction?>> = MutableLiveData()
    val returnfromproductionlist: LiveData<Array<ReturnFromProduction?>> = MutableLiveData()
    val invalidreturnissueProjects: Array<ReturnFromProduction?> = arrayOf(null)
    val returnfromprod: LiveData<Array<ReturnFromProduction?>> = MutableLiveData()
    val invalidreturnissueProject: Array<ReturnFromProductionResponse?> = arrayOf(null)
    val returnFromProductionResponse: LiveData<Array<ReturnFromProductionResponse?>> = MutableLiveData()
    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())
    var returnfromprodList = ArrayList<ReturnFromProduction?>()
    var scanedItems: List<ReturnFromProductionScanItem?> = emptyList()
    var liveList: LiveData<ReturnFromProductionResponse?> = MutableLiveData()
    var ReturnFromProduction = ArrayList<ReturnFromProduction?>()
    var errorMessage: String = ""


    fun loadReturnFromProductionItems(projectId: Int?,performedBy: Int?) {
        RemoteRepository.singleInstance.getReturnFromProduction(projectId,performedBy, this::handleReturnFromProductionResponseNext,
                this::handleReturnFromProductionErrorNext)
    }

    private fun handleReturnFromProductionResponseNext(returnfromproduction: Array<ReturnFromProduction?>) {
        println("after update->"+returnfromproduction.size)

        val found = returnfromproduction.filter{
                it!!.doneBy.employeeId.toString() == employeeId &&  it.projectId == projectId
                        && it.transactionType?.toLowerCase() == "issue to production"}

        for (i in found){
            if (i != null) {
                returnfromprodList.add(i)
            }
        }

        // scanned item at top
        for (i in scanedItems){
            val scannedItemFound = returnfromprodList?.filter{
                it!!.materialinward.barcodeSerial.toString() == i!!.barcodeSerial.toString()}
            if (scannedItemFound.size > 0){
                returnfromprodList.remove(scannedItemFound[0])
                returnfromprodList.add(0, scannedItemFound[0])
            }
        }


        (this.returnfromproduction as MutableLiveData<Array<ReturnFromProduction?>>).value = found.toTypedArray()
    }

    private fun handleReturnFromProductionErrorNext(error: Throwable) {
        Log.d(ContentValues.TAG, error.localizedMessage)
    }

    fun returnFromProductionPost(requestbody: Array<ReturnFromProductionScanItem?>){
        RemoteRepository.singleInstance.postReturnIssueToMaterial(requestbody,
                this::handleReturnToProdRes,
                this::handleReturnToProdError)

    }

    private fun handleReturnToProdRes(issuetoprod: Array<ReturnFromProductionResponse?>) {
        returnfromprodList = ArrayList<ReturnFromProduction?>()
        // println("success -->"+ QCScanResponse)
        deleteSelectedReturnFromProductionFromDB()
        // println("after delete call-->"+ QCScanResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
                loadReturnFromProductionItems(projectId,performedBy)
            }
        }
       // (this.returnfromprod as MutableLiveData<Array<ReturnFromProductionResponse?>>).value = issuetoprod
    }


    private fun handleReturnToProdError(error: Throwable) {
        Log.d(ContentValues.TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.returnfromprod as MutableLiveData<Array<ReturnFromProductionResponse?>>).value = invalidreturnissueProject
        }
    }

    suspend fun addReturnFromproductionItem(id: Int?,  projectId: Int?, quantity: Int?,
                                            remarks: String?, userId: Int?, barcodeSerial: String?){

        var dbItem = ReturnFromProductionItemList(
                id = null ,
                materialInwardId = id,
                projectId = projectId,
                quantity = quantity,
                remarks = remarks,
                userId = userId,
                barcodeSerial = barcodeSerial
        )
        println("insert query -->"+dbItem)
        var dbDao = appDatabase.returnFromProductionListItemDao()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getItemsFromDB()
            }
        }
    }

    fun getItemsFromDB(): List<ReturnFromProductionScanItem?> {
        var dbDao = appDatabase.returnFromProductionListItemDao()
        var dbItems = dbDao.getAllItems()
        scanedItems = dbItems
        println("--dbItems.size- return to prod->"+dbItems.size)
        var thisobj = this
        (thisobj.totalScannedItem as MutableLiveData<Number>).value = dbItems.size
        qcScannedCount = dbItems.size
         println("qcScannedCount-->"+qcScannedCount)
        return dbItems
    }

    fun deleteSelectedReturnFromProductionFromDB() {
        var dbDao = appDatabase.returnFromProductionListItemDao()

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dbDao.deleteAll()
                getItemsFromDB()
                loadReturnFromProductionItems(projectId,performedBy)
            }
        }
    }

    fun submitScanItem( remarks: String?=null) {
        var scanItem = getItemsFromDB()
        var toBeUpdatedItems: Array<ReturnFromProductionScanItem?> = emptyArray()

        for (item in scanItem){
            var scanItemObj = ReturnFromProductionScanItem()
//            scanItemObj.id = item!!.projectId
//            scanItemObj.remarks = item!!.remarks
//            scanItemObj.performedBy = item!!.performedBy
//            scanItemObj.projectId = item!!.projectId
//            scanItemObj.materialInwardId = item!!.materialInwardId
//            scanItemObj.quantity = item!!.quantity
            scanItemObj.materialInwardId = item!!.materialInwardId
            scanItemObj.remarks =item!!.remarks
            scanItemObj.quantity = item!!.quantity
            scanItemObj.projectId = item!!.projectId
            scanItemObj.userId = item!!.userId
            if (remarks !=null){
                scanItemObj.remarks = remarks
            }
            toBeUpdatedItems += scanItemObj
        }
        returnFromProductionPost(toBeUpdatedItems)
        // toBeUpdatedItems
    }
}
