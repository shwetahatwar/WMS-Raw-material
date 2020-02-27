package com.briot.balmerlawrie.implementor.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlip
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository

class DispatchSlipsViewModel : ViewModel() {
    val TAG = "DispatchLoadingListsVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val dispatchLoadingList: LiveData<Array<DispatchSlip?>> = MutableLiveData()
    val invalidDispatchList: Array<DispatchSlip?> = arrayOf(null)
    var userId: Int = 0

    fun loadDispatchLoadingLists(userId: Int) {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.dispatchLoadingList as MutableLiveData<Array<DispatchSlip?>>).value = null // emptyArray()

        RemoteRepository.singleInstance.getAssignedLoaderDispatchSlips(userId, this::handleDispatchLoadingListsResponse, this::handleLoadingListsError)
    }

    private fun handleDispatchLoadingListsResponse(dispatchLoadingList: Array<DispatchSlip?>) {
        Log.d(TAG, "successful selected loader's dispatch list details" + dispatchLoadingList.toString())
        (this.dispatchLoadingList as MutableLiveData<Array<DispatchSlip?>>).value = dispatchLoadingList
    }

    private fun handleLoadingListsError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.dispatchLoadingList as MutableLiveData<Array<DispatchSlip?>>).value = invalidDispatchList
        }
    }
}

