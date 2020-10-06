package com.briot.wms.implementor.ui.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.data.AppDatabase
import com.briot.wms.implementor.data.IssueToProduction
import com.briot.wms.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IssueToProductionViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var employeeId: String? = ""
    var username:String? = null
    var name: String? = null
    var Projectname: String? = null
    var id: Int? = null
    var userId: Int? = null
    var projectId: Int? = null
    var inputMaterialBarcode: String? = null
    var issueSlipStatus: String? = ""
    var errorMessage: String = ""
    var quantity: String? = null
    // var issueToProdList: List<MaterialData?> = emptyList()
    var issueToProdList = ArrayList<MaterialData>()


    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    var materialCount: Number? = 0
    val networkError: LiveData<Boolean> = MutableLiveData()
    val materialData: LiveData<Array<MaterialData?>> = MutableLiveData()
    val invalidProjects: Array<MaterialData?> = arrayOf(null)
    var scanedItems: List<IssueToProduction> = emptyList()
//    var scanedItems: LiveData<Array<IssueToProduction?>> = MutableLiveData()
    val totalScannedItem: LiveData<Number> = MutableLiveData()
    val issuetoproductionlist: LiveData<Array<IssueToProductionResponse?>> = MutableLiveData()
    val invalidissueProjects: Array<IssueToProductionResponse?> = arrayOf(null)

    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())


    fun loadMaterialItems(picklistId: Int?) {

        RemoteRepository.singleInstance.getIssueToProductionMaterial(picklistId, this::handleQCPendingItemsResponseNext,
                this::handleQCPendingItemsErrorNext)
    }

    private fun handleQCPendingItemsResponseNext(materialData: Array<MaterialData?>) {
        // println("after update putawayList.size-->"+qcpendingList.size)
        var thisobj = this
        for (i in materialData){
            if (i != null) {
                issueToProdList.add(i)
            }
        }
        // To show scanned item at top
        for (i in scanedItems){
            val scannedItemFound = issueToProdList.filter{
                it!!.batchNumber.toString() == i.inputMaterialBarcode.toString()}
            if (scannedItemFound != null){
                if (scannedItemFound?.size!! > 0){
                    issueToProdList.remove(scannedItemFound[0])
                    scannedItemFound[0].quantityPicked = i.quantity
                    issueToProdList.add(0, scannedItemFound[0])
                }
            }
        }

        (this.materialData as MutableLiveData<Array<MaterialData?>>).value = materialData
    }

    private fun handleQCPendingItemsErrorNext(error: Throwable) {
        Log.d(ContentValues.TAG, error.localizedMessage)
    }


    suspend fun addItemInDatabase(inputMaterialBarcode: String?, projectId: Int?,
                                  userId: Int?, picklistId: Int?,
                                  quantity: Int?, materialInwardId: Int?,
                                  picklistName: String?, employeeId:String?) {
        var dbItem = IssueToProduction(id = null,
                inputMaterialBarcode = inputMaterialBarcode,
                projectId = projectId,
                userId = userId,
                picklistId = picklistId,
                quantity = quantity,
                picklistName = picklistName,
                materialInwardId = materialInwardId,
                employeeId = employeeId
        )

//        println("viewModel.quantity--"+quantity)
        println("dbItem in insert query-->"+dbItem)
        var dbDao = appDatabase.issueToProductionDao()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getScanedItems(employeeId, picklistName)
            }
        }
    }

    fun getScanedItems(employeeId: String?, picklistName: String?) {
        println("get employeeId ->"+employeeId)
        println("get picklistName ->"+picklistName)
        var dbDao = appDatabase.issueToProductionDao()
        var dbItems = dbDao.getScanedItemsForUser(employeeId, picklistName)
        var all = dbDao.getAll()

        var thisobj = this
        if (dbItems != null) {
            scanedItems = dbItems
            // (this.scanedItems as MutableLiveData<Array<IssueToProduction?>>).value = dbItems.toTypedArray()
        }
        println("dbItems size in get call->"+dbItems?.size)
//        println("scanedItems->"+this.scanedItems.value?.size)

        for (i in all){
            println("all barcode ->"+i.inputMaterialBarcode)
            println("all employeeId ->"+i.employeeId)
            println("all picklistName ->"+i.picklistName)
            println("---------------------------------------------")
        }

        (thisobj.totalScannedItem as MutableLiveData<Number>).value = dbItems?.size
    }

    fun deleteScannedItem(picklistName: String?, employeeId: String?){
        var dbDao = appDatabase.issueToProductionDao()

        println("get call b4 delete")
        getScanedItems(employeeId, picklistName)
        println("get end --------------------------")
        println("inside delete----")
        GlobalScope.launch {
            dbDao.deleteScannedItem(picklistName, employeeId)
            println("after delete call ")
            withContext(Dispatchers.Main) {
                getScanedItems(employeeId, picklistName)
                issueToProdList = ArrayList<MaterialData>()
                loadMaterialItems(id)
            }
        }

    }


    fun deleteScannedItemWithBarcode(employeeId: String?, picklistName: String?, inputMaterialBarcode: String?){
        var dbDao = appDatabase.issueToProductionDao()
        // var dbItems = dbDao.deleteScannedItem(picklistName,employeeId)

//        println("-input employeeId->"+employeeId)
//        println("-input picklistName->"+picklistName)
//        println("-input inputMaterialBarcode->"+inputMaterialBarcode)
//
//        println("get call b4 delete")
        getScanedItems(employeeId, picklistName)
//        println("get end --------------------------")
//        println("inside delete----")
        GlobalScope.launch {
            dbDao.deleteScannedItemWithBarcode(employeeId, picklistName, inputMaterialBarcode)

            // dbDao.updateValue(employeeId, picklistName)
            withContext(Dispatchers.Main) {
                getScanedItems(employeeId, picklistName)
                issueToProdList = ArrayList<MaterialData>()
                loadMaterialItems(id)
            }
        }
//        getScanedItems(picklistName, employeeId)
//        loadMaterialItems(id)
    }


    fun submitIssueToProd(){
        var itemToUpdate: Array<IssueToProductionList> = emptyArray()

        for (i in scanedItems!!){
           var itemToAdd =  IssueToProductionList()
            itemToAdd.barcodeSerial = i!!.inputMaterialBarcode
            itemToAdd.materialInwardId = i!!.materialInwardId
            itemToAdd.picklistId = i!!.picklistId
            itemToAdd.projectId = i!!.projectId
            itemToAdd.quantity = i!!.quantity
            itemToAdd.userId = i!!.userId
            itemToUpdate += itemToAdd
        }
        println("itemToUpdate-->"+itemToUpdate)
        RemoteRepository.singleInstance.postIssueToMaterial(itemToUpdate,
                                                        this::handleIssueToProdRes,
                                                        this::handleIssueToProdError)

    }

    private fun handleIssueToProdRes(issuetoprod: Array<IssueToProductionResponse?>) {


        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                deleteScannedItem(name, employeeId)
                getScanedItems(employeeId, name)
            }
        }
        (this.issuetoproductionlist as MutableLiveData<Array<IssueToProductionResponse?>>).value = issuetoprod
    }


    private fun handleIssueToProdError(error: Throwable) {
        Log.d(TAG, "error msg--->"+error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            println("------inside network error----")
            errorMessage = error.message.toString()
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.issuetoproductionlist as MutableLiveData<Array<IssueToProductionResponse?>>).value = invalidissueProjects
        }
    }
}