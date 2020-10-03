package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.*
import java.util.ArrayList
import java.util.Comparator

class PicklistMasterViewModel : ViewModel() {
    val TAG = "AuditProjectsVM"

    val networkError: LiveData<Boolean> = MutableLiveData()
    val picklistMasterData: LiveData<Array<picklistMasterRes?>> = MutableLiveData()
    val invalidProjects: Array<picklistMasterRes?> = arrayOf(null)
    var picklistMasterCount: Number? = 0
    var picklistMasterResList = ArrayList<picklistMasterRes>()


    fun loadpicklistTotalCount(){
        RemoteRepository.singleInstance.getPickListCount(this::handlePickMasterCountResponse,
                this::handlePickMasterCountError)
    }


    private fun handlePickMasterCountResponse(picklistmaster: picklistMasterTotalCount?) {
        // Log.d(TAG, "successful project list" + projects.toString())
        picklistMasterCount = picklistmaster!!.total!!.toInt()
        //(this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = projects
    }


    private fun handlePickMasterCountError(error: Throwable) {
        Log.d(TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = invalidProjects
        }
    }


    fun loadpicklistMasterData(offset: String){
        RemoteRepository.singleInstance.gePickListMaster(offset,
                this::handlePickMasterResponse, this::handlePickMasterError)
    }

    private fun handlePickMasterResponse(projects: Array<picklistMasterRes?>) {
        for (i in projects){
            if (i != null) {
                picklistMasterResList.add(i)
            }
        }
        // picklistMasterResList.sortByDescending { it.picklistStatus }


        picklistMasterResList.sortWith(compareBy<picklistMasterRes?> {
            (it!!.picklistStatus.toString().toLowerCase() == "completed")
        }.thenBy {
            (it!!.picklistStatus.toString().toLowerCase() == "pending")
        }.thenBy {
            (it!!.picklistStatus.toString().toLowerCase() == "in progress")
        })


        (this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = projects
    }


    private fun handlePickMasterError(error: Throwable) {
        Log.d(TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.picklistMasterData as MutableLiveData<Array<picklistMasterRes?>>).value = invalidProjects
        }
    }
}
