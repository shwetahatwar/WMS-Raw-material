package com.briot.wms.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.repository.remote.Employee
import com.briot.wms.implementor.repository.remote.RemoteRepository

class IssueToProductionEmployeeViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val employeeScan: LiveData<Array<Employee?>> = MutableLiveData()
    var employeeId: String? = ""
    var name:String? = null
    var projectName:String? = null
    var picklistName: String? = null
    var id: Int? = null
    var projectId: Int? = null


    fun loadEmployeeScanItems(employeeId: String) {
        RemoteRepository.singleInstance.getEmployeeScan(employeeId, this::handleEmployeeScanResponse,
                this::handleEmployeeScanError)
    }

    private fun handleEmployeeScanResponse(employeeidScan: Array<Employee?>) {
        println("putawayLocation - >"+employeeidScan)
        for (i in employeeidScan){
            println("item - >"+i?.employeeId)
        }
        (this.employeeScan as MutableLiveData<Array<Employee?>>).value = employeeidScan
    }

    private fun handleEmployeeScanError(error: Throwable) {
        Log.d(ContentValues.TAG, "error -->"+error.localizedMessage)
    }
}