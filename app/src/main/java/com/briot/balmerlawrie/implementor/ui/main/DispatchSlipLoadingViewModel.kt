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
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
import kotlinx.coroutines.CoroutineScope
import java.util.*

class DispatchSlipLoadingViewModel : ViewModel() {

    var dispatchSlipId: Int = 0
    var dispatchSlipNumber: String? = ""
    var dispatchSlipVehicleNumber: String? = ""
    var dispatchSlipStatus: String? = ""
    var userId: Int = 0
    var dispatchSlipTruckId: Int = 0

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

        for (item in updatedItems) {
            if (item != null) {
                var count = dbDao.getCountForBatchMaterialCode(
                        dispatchSlipId,
                        item.materialCode!!,
                        item.batchNumber!!
                )
                item.scannedPacks = count
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

        updatedItems.sortWith(compareBy<DispatchSlipItem?> { it!!.scannedPacks == 0}.thenBy { (it!!.scannedPacks.toInt() < it!!.numberOfPacks.toInt()) }.thenBy { it!!.scannedPacks == it!!.numberOfPacks })
        //

        (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = updatedItems
    }

    private suspend fun updateItemInDatabase(item: DispatchSlipItem) {

        var dbItem = DispatchSlipLoadingListItem(
                batchCode = item.batchNumber,
                productCode = item.materialCode,
                dispatchSlipId = item.dispatchSlipId!!.toInt(),
                dipatchSlipNumber = dispatchSlipNumber,
                timeStamp = Date().time,
                serialNumber = null,
                vehicleNumber = dispatchSlipVehicleNumber, id = 0)

        var dbDao = appDatabase.dispatchSlipLoadingItemDuo()
        dbDao.insert(item = dbItem)

        updatedListAsPerDatabase(responseDispatchLoadingItems)
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

    }
}
