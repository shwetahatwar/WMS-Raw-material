package com.briot.wms.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.RemoteRepository
import com.briot.wms.implementor.repository.remote.picklistMasterRes
import java.util.ArrayList

class IssueToProductionMasterViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val networkError: LiveData<Boolean> = MutableLiveData()
    val picklistMasterData: LiveData<Array<picklistMasterRes?>> = MutableLiveData()
    val invalidProjects: Array<picklistMasterRes?> = arrayOf(null)
    var picklistMasterCount: Number? = 0
    var picklistMasterResList = ArrayList<picklistMasterRes>()
    var projectId: Int? = null
    var name: String? = null

    fun loadIssueToProductionlistMasterData(offset: String){
        RemoteRepository.singleInstance.getIssueToProductionMaster(offset,
                this::handleIssueToProductionMasterResponse, this::handleIssueToProductionMasterError)
    }

    private fun handleIssueToProductionMasterResponse(projects: Array<picklistMasterRes?>) {
        for (i in projects){
            if (i != null) {
                picklistMasterResList.add(i)
            }
        }
        (this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = projects
    }


    private fun handleIssueToProductionMasterError(error: Throwable) {
        Log.d(TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = invalidProjects
        }
    }
}