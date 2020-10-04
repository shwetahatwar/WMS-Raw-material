package com.briot.wms.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.data.AppDatabase
import com.briot.wms.implementor.data.QCScanItemList
import com.briot.wms.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.util.*
import kotlin.collections.ArrayList

class QCPendingViewModel : ViewModel() {

    var description: String? = null
    var barcodeSerial: String? = null
    var partnumber: Number? = null
    var qcTotalCount: Number? = 0
    var qcScannedCount: Number? = 0
    var pending: Number? = null
    var QCRemarks: String? = null
    var qcpendingList = ArrayList<QCPending>()
    var errorMessage: String = ""

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val QCPendingItem: LiveData<Array<QCPending?>> = MutableLiveData()
    val totalScannedItem: LiveData<Number> = MutableLiveData()
    var scanedItems: List<QCScanItem> = emptyList()
    var liveList: LiveData<QCScanItem> = MutableLiveData()

    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())

    fun getQcTotalCount() {
        RemoteRepository.singleInstance.getQcTotalCount(
                this::handleQCTotalCount, this::handleQCTotalCountError)
    }

    private fun handleQCTotalCount(QcTotalCount: QCTotalCount?) {
        qcTotalCount = QcTotalCount!!.QCStatus.pending
}

    private fun handleQCTotalCountError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
    }

    fun loadQCPendingItems(limit: String,offset: String) {
        (networkError as MutableLiveData<Boolean>).value = false
        println("----load count ->"+limit)
        RemoteRepository.singleInstance.getQcPendingCount(limit, offset, this::handleQCPendingItemsResponseNext,
                this::handleQCPendingItemsErrorNext)
    }

    private fun handleQCPendingItemsResponseNext(QCPendingItem: Array<QCPending?>) {
        // println("QCPendingItem response-->"+QCPendingItem)
        // println("qcpendingList.size-->"+qcpendingList.size)

        var thisobj = this
        for (i in QCPendingItem){
            if (i != null) {
                qcpendingList.add(i)
            }
        }

        // scanned item at top
        for (i in scanedItems){
            val scannedItemFound = qcpendingList?.filter{
                it!!.barcodeSerial.toString() == i.barcodeSerial.toString()}
            if (scannedItemFound.size > 0){
                qcpendingList.remove(scannedItemFound[0])
                qcpendingList.add(0, scannedItemFound[0])
            }
        }

       // println("after update putawayList.size-->"+qcpendingList.size)
        (thisobj.QCPendingItem as MutableLiveData<Array<QCPending?>>).value = QCPendingItem
    }

    private fun handleQCPendingItemsErrorNext(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
    }

    fun updateQcScanItem(qcScanRequestBody: Array<QCScanItem>) {
        RemoteRepository.singleInstance.postQcPendingScanItems(qcScanRequestBody,
                this::handleQCScanResponse, this::handleQCScanError)
    }

    private fun handleQCScanResponse(QCScanResponse: ResponseBody) {
        qcpendingList = ArrayList<QCPending>()
        // println("success -->"+ QCScanResponse)
        deleteQCPendingScanItemsFromDB()
        // println("after delete call-->"+ QCScanResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
                getQcTotalCount()
            }
        }
    }

    private fun handleQCScanError(error: Throwable) {
        Log.d(TAG, "error --->"+error.localizedMessage)
    }

    suspend fun addQcScanItem(barcodeSerial: String, id: Int?, prevQCStatus: Int?){
        val QCScanItemObj = QCScanItem()
        QCScanItemObj.barcodeSerial = barcodeSerial
        QCScanItemObj.QCStatus = prevQCStatus
        QCScanItemObj.prevQCStatus = prevQCStatus
        QCScanItemObj.qcId = id
        // deleteQCPendingScanItemsFromDB()
        updateDAtabase(QCScanItemObj)
    }

    private suspend fun updateDAtabase(item: QCScanItem){
        var dbItem = QCScanItemList(
                id = null ,
                barcodeSerial = item.barcodeSerial,
                qcId = item.qcId,
                prevQCStatus = item.prevQCStatus,
                QCStatus = item.prevQCStatus,
                timeStamp = Date().time
        )
        println("insert query -->"+dbItem)
        var dbDao = appDatabase.qcPendingScanListItemDao()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getItemsFromDB()
            }
        }
    }

    fun getItemsFromDB(): List<QCScanItem> {
        var dbDao = appDatabase.qcPendingScanListItemDao()
        var dbItems = dbDao.getAllItems()

//        for (i in dbItems){
//            println("--------------------------------")
//            println("barcodeSerial-->"+i.barcodeSerial)
//            println("qcid-->"+i.qcId)
//        }
        scanedItems = dbItems
        var thisobj = this
        (thisobj.totalScannedItem as MutableLiveData<Number>).value = dbItems.size
        qcScannedCount = dbItems.size
        // println("qcScannedCount-->"+qcScannedCount)
        return dbItems
    }

    fun deleteQCPendingScanItemsFromDB() {
        var dbDao = appDatabase.qcPendingScanListItemDao()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dbDao.deleteAll()
                getItemsFromDB()
                qcpendingList = ArrayList<QCPending>()
                loadQCPendingItems("100","0")
                getQcTotalCount()
            }
        }
    }

    fun submitScanItem(qcStatus: Int, QCRemarks: String?=null) {
        // get data from room database
        // post call with qcStatus
        var scanItem = getItemsFromDB()
        var toBeUpdatedItems: Array<QCScanItem> = emptyArray()

        for (item in scanItem){
            var scanItemObj = QCScanItem()
            scanItemObj.id = item.qcId
            scanItemObj.barcodeSerial = item.barcodeSerial
            scanItemObj.prevQCStatus = item.prevQCStatus
            scanItemObj.QCStatus = qcStatus
            if (QCRemarks !=null){
                scanItemObj.QCRemarks = QCRemarks
            }
            toBeUpdatedItems += scanItemObj
        }
        updateQcScanItem(toBeUpdatedItems)
        // toBeUpdatedItems.sortByDescending { it?.timestamp}
        // toBeUpdatedItems
    }
}
