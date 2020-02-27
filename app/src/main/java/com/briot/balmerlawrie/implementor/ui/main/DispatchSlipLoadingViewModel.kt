package com.briot.balmerlawrie.implementor.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.data.AppDatabase
import com.briot.balmerlawrie.implementor.data.DispatchSlipLoadingListItem
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItem
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItemRequest
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipRequest
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
import kotlinx.coroutines.*
import java.util.*

class DispatchSlipLoadingViewModel : ViewModel() {

    var dispatchSlipId: Int = 0
    var dispatchSlipNumber: String? = ""
    var dispatchSlipVehicleNumber: String? = ""
    var dispatchSlipStatus: String? = ""
    var userId: Int = 0
    var dispatchSlipTruckId: Int = 0
    var totalScannedItems: Int = 0

    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())

    val TAG = "DispatchLoadingListVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val dispatchloadingItems: LiveData<Array<DispatchSlipItem?>> = MutableLiveData()
    private var responseDispatchLoadingItems: Array<DispatchSlipItem?> = arrayOf(null)
    val invalidDispatchloadingItems: Array<DispatchSlipItem?> = arrayOf(null)

    fun loadDispatchSlipLoadingItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = emptyArray()

        RemoteRepository.singleInstance.getDispatchSlipItems(dispatchSlipId, this::handleDispatchLoadingItemsResponse, this::handleDispatchLoadingItemsError)
    }

    private fun handleDispatchLoadingItemsResponse(dispatchSlipItems: Array<DispatchSlipItem?>) {
        Log.d(TAG, "successful dispatch loading items details" + dispatchSlipItems.toString())

        responseDispatchLoadingItems = dispatchSlipItems
        updatedListAsPerDatabase(responseDispatchLoadingItems)

    }

    private fun updatedListAsPerDatabase(items: Array<DispatchSlipItem?>) {

        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()
        var dbItems = dbDao.getAllDispatchSlipItems(dispatchSlipId)

        var updatedItems: Array<DispatchSlipItem?> = items.clone()

        totalScannedItems = 0
        for (item in updatedItems) {
            if (item != null) {
                var count = dbDao.getCountForBatchMaterialCode(
                        dispatchSlipId,
                        item.materialCode!!,
                        item.batchNumber!!
                )
                item.scannedPacks = count
                if (item.scannedPacks.toInt() == item.numberOfPacks.toInt()) {
                    totalScannedItems += 1
                }
            }
        }

        /*
//        var differentItems = Array<DispatchSlipItem>()

        for (dbItem in dbItems.value!!.iterator()) {
            var foundItems = updatedItems.filter {
                return  (dbItem.batchCode.equals(it?.batchNumber))
            }

            if (foundItems!!.size == 0) {
                var item = DispatchSlipItem()
                item.id = 0
                item.scannedPacks = 1
                item.batchNumber = dbItem.batchCode
                item.materialCode = dbItem.productCode
                item.dispatchSlipId = dbItem.dispatchSlipId
                item.numberOfPacks = 1
                differentItems.add   (item)
            }
//            for (item in updatedItems) {
//                if (item != null) {
//                    var dbBatchwiseItems = dbDao.getItemsForBatch(dispatchSlipId, item!!.batchNumber)
//                }
//            }

        }*/

        updatedItems.sortWith(compareBy<DispatchSlipItem?> {
            it!!.scannedPacks.toInt() == it!!.numberOfPacks.toInt()
        }.thenBy {
            (it!!.scannedPacks.toInt() < it!!.numberOfPacks.toInt())
        }.thenBy {
            it!!.scannedPacks == 0
        })
        //

        (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = updatedItems
    }

    fun isMaterialBelongToSameGroup(materialCode: String, batchNumber: String): Boolean {
        val result = responseDispatchLoadingItems.filter {
            (it?.materialCode.equals(materialCode) && it?.batchNumber.equals(batchNumber))
        }
        return (result.size > 0)
    }

    fun materialQuantityPickingCompleted(materialCode: String, batchNumber: String): Boolean {

        val result = responseDispatchLoadingItems.filter {
            (it?.materialCode.equals(materialCode) && it?.batchNumber.equals(batchNumber))
        }

        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()

        for (item in result) {
            if (item != null) {
                var count = dbDao.getCountForBatchMaterialCode(
                        dispatchSlipId,
                        item.materialCode!!,
                        item.batchNumber!!
                )
                item.scannedPacks = count
                if (item.scannedPacks.toInt() == item.numberOfPacks.toInt()) {
                    return true
                } else {
                    return false
                }
            }
        }

        return false
    }

    fun isSameSerialNumber(materialCode: String, batchNumber: String, serialNumber: String): Boolean {

        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()

        var count = dbDao.getCountForBatchMaterialCodeSerial(
                dispatchSlipId,
                materialCode,
                batchNumber,
                serialNumber
        )

        if (count > 0) {
            return true
        } else {
            return false
        }
    }



    suspend fun addMaterial(materialCode: String, batchNumber: String, serialNumber: String): Boolean {
        val result = responseDispatchLoadingItems.filter {
            it?.materialCode.equals(materialCode) && it?.batchNumber.equals(batchNumber)
        }

        if (result.size > 0) {
            updateItemInDatabase(result.first()!!, serialNumber)
        }
        return true
    }

    private suspend fun updateItemInDatabase(item: DispatchSlipItem, serialNumber: String) {

        var dbItem = DispatchSlipLoadingListItem(
                batchCode = item.batchNumber,
                productCode = item.materialCode,
                dispatchSlipId = item.dispatchSlipId!!.toInt(),
                dipatchSlipNumber = dispatchSlipNumber,
                timeStamp = Date().time,
                serialNumber = serialNumber,
                vehicleNumber = dispatchSlipVehicleNumber, id = 0, submitted = 0)


        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()
        dbDao.insert(item = dbItem)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                updatedListAsPerDatabase(responseDispatchLoadingItems)
            }
        }
    }


    private fun handleDispatchLoadingItemsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = invalidDispatchloadingItems
        }
    }



    private fun handleSubmitLoadingList() {
        var dispatchSlipRequestObject = DispatchSlipRequest()
        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()
        var dbItems = dbDao.getAllDispatchSlipItems(
                dispatchSlipId
        )

        var items = mutableListOf<DispatchSlipItemRequest>()
        var startTime = ""
        var endTime = ""
        if (dbItems.value != null) {
            startTime = Date(dbItems.value!!.first().timeStamp).toString()
            endTime = Date(dbItems.value!!.last().timeStamp).toString()

            for (dbItem in dbItems.value!!.iterator()) {
                var item = DispatchSlipItemRequest()
                item.batchNumber = dbItem.batchCode
                item.materialCode = dbItem.productCode
                item.serialNumber = dbItem.serialNumber
                items.add(item)
            }
        }


        dispatchSlipRequestObject.dispatchId = dispatchSlipId
        dispatchSlipRequestObject.truckNumber = dispatchSlipVehicleNumber
        dispatchSlipRequestObject.truckId = dispatchSlipTruckId
        dispatchSlipRequestObject.loadStartTime = startTime
        dispatchSlipRequestObject.loadEndTime = endTime
        dispatchSlipRequestObject.material = items.toTypedArray()

    }
}
