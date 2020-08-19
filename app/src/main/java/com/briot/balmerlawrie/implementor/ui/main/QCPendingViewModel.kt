package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.data.AppDatabase
import com.briot.balmerlawrie.implementor.data.QCScanItemList
import com.briot.balmerlawrie.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QCPendingViewModel : ViewModel() {

    var description: String? = null
    var barcodeSerial: String? = null
    var partnumber: Number? = null
    var qcTotalCount: Number? = 0
    var qcScannedCount: Number? = 0
    var pending: Number? = null

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val QCPendingItem: LiveData<Array<QCPending?>> = MutableLiveData()
    var scanedItems: List<QCScanItem> = emptyList()
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

    fun loadQCPendingItems(limit: String) {
        RemoteRepository.singleInstance.getQcPendingCount(limit, this::handleQCPendingItemsResponseNext,
                this::handleQCPendingItemsErrorNext)
    }

    private fun handleQCPendingItemsResponseNext(QCPendingItem: Array<QCPending?>) {
        var thisobj = this
        (thisobj.QCPendingItem as MutableLiveData<Array<QCPending?>>).value = QCPendingItem
    }

    private fun handleQCPendingItemsErrorNext(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
    }


    fun updateQcScanItem(qcScanRequestBody: Array<QCScanItem>) {
        RemoteRepository.singleInstance.postQcPendingScanItems(qcScanRequestBody,
                this::handleQCScanResponse, this::handleQCScanError)
    }

    private fun handleQCScanResponse(success: qcScanResponse?) {
        println("success -->"+ success)
        deleteQCPendingScanItemsFromDB()
        println("after delete call-->"+ success)
    }

    private fun handleQCScanError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
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
                QCStatus = item.prevQCStatus
        )
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
        scanedItems = dbItems
        qcScannedCount = dbItems.size
        println("qcScannedCount-->"+qcScannedCount)
        return dbItems
    }

    fun deleteQCPendingScanItemsFromDB() {
        var dbDao = appDatabase.qcPendingScanListItemDao()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dbDao.deleteAll()
                //updatedListAsPerDatabase()
            }
            getItemsFromDB()
        }
    }

    fun submitScanItem(qcStatus: Int) {
        // get data from room database
        // post call with qcStatus
        var scanItem = getItemsFromDB()
        var scanItemObj = QCScanItem()
        var toBeUpdatedItems: Array<QCScanItem> = emptyArray()

        for (item in scanItem){
            scanItemObj.id = item.qcId
            scanItemObj.barcodeSerial = item.barcodeSerial
            scanItemObj.prevQCStatus = item.prevQCStatus
            scanItemObj.QCStatus = qcStatus
            toBeUpdatedItems += scanItemObj
        }
        updateQcScanItem(toBeUpdatedItems)
        // toBeUpdatedItems
    }
}
