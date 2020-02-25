package com.briot.balmerlawrie.implementor.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItem
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository

class DispatchPickingListDetailsViewModel : ViewModel() {


    var dispatchSlipId: Int = 0
    var dispatchSlipNumber: String? = ""
    var dispatchSlipVehicleNumber: String? = ""
    var dispatchSlipStatus: String? = ""
    var userId: Int = 0
    var dispatchSlipTruckId: Int = 0

    val TAG = "DispatchPickingListVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val dispatchPickingItems: LiveData<Array<DispatchSlipItem?>> = MutableLiveData()
    val invalidDispatchPickingItems: Array<DispatchSlipItem?> = arrayOf(null)

    fun loadDispatchSlipPickingItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.dispatchPickingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = emptyArray()

        RemoteRepository.singleInstance.getDispatchSlipItems(dispatchSlipId, this::handleDispatchPickingItemsResponse, this::handleDispatchPickingItemsError)
    }

    private fun handleDispatchPickingItemsResponse(dispatchSlipItems: Array<DispatchSlipItem?>) {
        Log.d(TAG, "successful dispatch picking items details" + dispatchSlipItems.toString())
        (this.dispatchPickingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = dispatchSlipItems
    }

    private fun handleDispatchPickingItemsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.dispatchPickingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = invalidDispatchPickingItems
        }
    }
}
