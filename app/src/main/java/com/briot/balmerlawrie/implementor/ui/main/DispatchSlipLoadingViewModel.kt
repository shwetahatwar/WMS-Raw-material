package com.briot.balmerlawrie.implementor.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItem
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository

class DispatchSlipLoadingViewModel : ViewModel() {

    var dispatchSlipId: Int = 0
    var dispatchSlipNumber: String? = ""
    var dispatchSlipVehicleNumber: String? = ""
    var dispatchSlipStatus: String? = ""
    var userId: Int = 0

    val TAG = "DispatchLoadingListVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val dispatchloadingItems: LiveData<Array<DispatchSlipItem?>> = MutableLiveData()
    val invalidDispatchloadingItems: Array<DispatchSlipItem?> = arrayOf(null)

    fun loadDispatchSlipLoadingItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = emptyArray()

        RemoteRepository.singleInstance.getDispatchSlipItems(dispatchSlipId, this::handleDispatchLoadingItemsResponse, this::handleDispatchLoadingItemsError)
    }

    private fun handleDispatchLoadingItemsResponse(dispatchSlipItems: Array<DispatchSlipItem?>) {
        Log.d(TAG, "successful dispatch loading items details" + dispatchSlipItems.toString())
        (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = dispatchSlipItems
    }

    private fun handleDispatchLoadingItemsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.dispatchloadingItems as MutableLiveData<Array<DispatchSlipItem?>>).value = invalidDispatchloadingItems
        }
    }



}
