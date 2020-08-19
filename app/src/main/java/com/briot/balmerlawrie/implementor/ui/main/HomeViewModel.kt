package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.PickingDashboardData
import com.briot.balmerlawrie.implementor.repository.remote.PutawayDashboardData
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
//import com.briot.balmerlawrie.implementor.repository.remote.RoleAccessRelation
import java.net.SocketException
import java.net.SocketTimeoutException

class HomeViewModel : ViewModel() {
    val TAG = "HomeViewModel"
    val networkError: LiveData<Boolean> = MutableLiveData<Boolean>()

    var totalData: Number? = null
    var pendingForPutaway: Number? = null

    var inProgress: Number? = null
    var pending: Number? = null
    var completed: Number? = null
    var total: Number? = null

    val putawayDashboardData: LiveData<PutawayDashboardData?> = MutableLiveData()
    val pickingDashboardData: LiveData<PickingDashboardData?> = MutableLiveData()

    val invalidPickingDashboardData: Array<PickingDashboardData?> = arrayOf(null)
    val invalidPutawayDashboardData: Array<PutawayDashboardData?> = arrayOf(null)

    var getResponsePutwayDashboardData: LiveData<PutawayDashboardData?> = MutableLiveData()

    fun loadPutawayDashboardItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.putawayDashboardData as MutableLiveData<PutawayDashboardData>).value = null

        RemoteRepository.singleInstance.getPutawayCount(this::handlePutawayDashboardItemsResponse, this::handlePutawayDashboardItemsError)
    }

    private fun handlePutawayDashboardItemsResponse(putawayDashboardData: PutawayDashboardData?) {
        (this.putawayDashboardData as MutableLiveData<PutawayDashboardData>).value = putawayDashboardData
    }

    private fun handlePutawayDashboardItemsError(error: Throwable) {
        Log.d(ContentValues.TAG, "-----in error"+ error)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.putawayDashboardData as MutableLiveData<Array<PutawayDashboardData?>>).value = invalidPutawayDashboardData
        }
    }


    fun loadPickingsDashboardItems() {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.pickingDashboardData as MutableLiveData<PickingDashboardData>).value = null

        RemoteRepository.singleInstance.getPickingCount(this::handlePickingDashboardItemsResponse,
                this::handlePickingDashboardItemsError)
    }

    private fun handlePickingDashboardItemsResponse(pickingsDashboardData: PickingDashboardData?) {
        // Setting response data to display
        inProgress = pickingsDashboardData!!.inProgress
        pending = pickingsDashboardData!!.pending
        completed = pickingsDashboardData!!.completed
        total = pickingsDashboardData!!.total

        (this.pickingDashboardData as MutableLiveData<PickingDashboardData>).value = pickingsDashboardData
    }

    private fun handlePickingDashboardItemsError(error: Throwable) {
        Log.d(ContentValues.TAG, "-----in error"+ error)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.pickingDashboardData as MutableLiveData<Array<PickingDashboardData?>>).value = invalidPickingDashboardData
        }
    }


}


