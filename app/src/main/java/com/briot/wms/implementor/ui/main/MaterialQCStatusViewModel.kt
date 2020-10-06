package com.briot.wms.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.data.AppDatabase
import com.briot.wms.implementor.data.MaterialQCStatusDB
import com.briot.wms.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class MaterialQCStatusViewModel : ViewModel() {

    val invalidmaterialItem: Array<MaterialInward?> = arrayOf(null)
    val networkError: LiveData<Boolean> = MutableLiveData()
    var materialInwards: LiveData<Array<MaterialInward?>> = MutableLiveData()
    var qcStatusDisplay: LiveData<Array<MaterialInward?>> = MutableLiveData()
    var scanedItems: List<QCScanItem> = emptyList()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    var barcodeSerial: String? = ""
    var errorMessage: String = ""
    var QCRemarks: String = ""
    var Q: String? = ""
    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())

    fun loadMaterialStatusItems(barcodeSerial: String) {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.materialInwards as MutableLiveData<Array<MaterialInward?>>).value = emptyArray()
        RemoteRepository.singleInstance.getMaterialStatus(barcodeSerial, this::handleMaterialItemResponse,
                this::handleMaterialItemError)
    }

    private fun handleMaterialItemResponse(res: Array<MaterialInward?>) {

        (this.qcStatusDisplay as MutableLiveData<Array<MaterialInward?>>).value = res
        barcodeSerial?.let { loadMaterialStatusItems(it) }
//        GlobalScope.launch {
//            withContext(Dispatchers.Main) {
//                addQcScanItem(res[0])
//            }
//        }
    }

    private fun handleMaterialItemError(error: Throwable) {
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.qcStatusDisplay as MutableLiveData<Array<MaterialInward?>>).value = invalidmaterialItem
        }
    }

    fun updateQcScanItem(qcScanRequestBody: Array<QCScanItem>) {
        RemoteRepository.singleInstance.postQcPendingScanItems(qcScanRequestBody,
                this::handleQCScanResponse, this::handleQCScanError)
    }

    private fun handleQCScanResponse(QCScanResponse: ResponseBody) {
        println("success -->"+ QCScanResponse)
        // deleteQCPendingScanItemsFromDB()
        // println("after delete call-->"+ QCScanResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
    }

    private fun handleQCScanError(error: Throwable) {
        Log.d(TAG, "error --->"+error.localizedMessage)
    }


    suspend fun addQcScanItem(matQcStatus: MaterialInward?){
        val materialQCStatus = MaterialInward()
        materialQCStatus.barcodeSerial = matQcStatus?.barcodeSerial
        materialQCStatus.QCStatus = matQcStatus?.QCStatus
        materialQCStatus.id = matQcStatus?.id
        materialQCStatus.partnumber.partNumber = matQcStatus?.partnumber?.partNumber
        materialQCStatus.partnumber.description = matQcStatus?.partnumber?.description
        materialQCStatus.shelf.name = matQcStatus?.shelf?.name
        updateDAtabase(materialQCStatus)
    }

    private fun convertIntToReadbleQCStatus(qcStatus: Int?): String {
        var result: String = ""
        if (qcStatus == 1){
            result = "QC Approved"
        }else if (qcStatus == 2){
            result = "QC Rejected"
        }else if (qcStatus == 0){
            result = "QC Pending"
        }
        return result
    }


    private fun convertReadbleToIntQCStatus(qcStatus: String?): Int? {
        var result: Int? = null
        if (qcStatus == "QC Approved"){
            result = 1
        }else if (qcStatus == "QC Rejected"){
            result = 2
        }else if (qcStatus == "QC Pending"){
            result = 0
        }
        return result
    }

    private suspend fun updateDAtabase(item: MaterialInward){
        var dbItem = MaterialQCStatusDB(
                id = null ,
                barcodeSerial = item.barcodeSerial,
                partnumber = item.partnumber.partNumber,
                description = item.partnumber.description,
                qcId = item.id,
                location = item.shelf.name,
                QCStatus = convertIntToReadbleQCStatus(item.QCStatus)
        )
        var dbDao = appDatabase.materialQCStatusDao()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getItemsFromDB()
            }
        }
    }

    fun getItemsFromDB() {
        var dbDao = appDatabase.materialQCStatusDao()
        var dbItems = dbDao.getAllItems()
        (this.qcStatusDisplay as MutableLiveData<Array<qcStatusDisplay?>>).value = dbItems
    }

    fun deleteQCPendingScanItemsFromDB() {
        var dbDao = appDatabase.materialQCStatusDao()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dbDao.deleteAll()
                getItemsFromDB()
            }
        }
    }

    fun submitScanItem(qcStatus: Int, remark: String="Change status") {
        var toBeUpdatedItems: Array<QCScanItem> = emptyArray()
        var scanItemObj = QCScanItem()

        for (item in this.qcStatusDisplay.value!!){
            // println(item?.qcId)
            scanItemObj.id = item?.id
            scanItemObj.barcodeSerial = item?.barcodeSerial
            scanItemObj.prevQCStatus = item?.QCStatus
            scanItemObj.QCStatus = qcStatus
            toBeUpdatedItems += scanItemObj
        }
        updateQcScanItem(toBeUpdatedItems)
    }
}

