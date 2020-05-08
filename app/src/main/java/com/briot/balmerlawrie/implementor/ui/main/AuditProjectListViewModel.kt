package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.Project
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
import com.briot.balmerlawrie.implementor.repository.remote.auditProjectItem
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.net.SocketException
import java.net.SocketTimeoutException

class AuditProjectListViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var errorMessage: String = ""
    var material_barcode = ""
    var batch_number = ""
    var barcode_serial = ""
    var projectId: Number? = null
    val networkError: LiveData<Boolean> = MutableLiveData()
    val projects: LiveData<Array<Project?>> = MutableLiveData()
    val auditProject: LiveData<Array<auditProjectItem?>> = MutableLiveData()
    val invalidProjects: Array<Project?> = arrayOf(null)

    fun updateAuditProjects(auditRequestBody: auditProjectItem) {

        RemoteRepository.singleInstance.postProjectItems(arrayOf(auditRequestBody),
                this::handleAuditProjectsResponse, this::handleProjectsError)
    }

    private fun handleAuditProjectsResponse(auditProject: Array<auditProjectItem?>) {
        Log.d(TAG, "successful project list" + auditProject.toString())
        (this.projects as MutableLiveData<Array<auditProjectItem?>>).value = auditProject
    }

    fun loadAuditProjects(status: String) {
        (networkError as MutableLiveData<Boolean>).value = false
        (this.projects as MutableLiveData<Array<Project?>>).value = emptyArray()

        RemoteRepository.singleInstance.getProjects(status, this::handleProjectsResponse, this::handleProjectsError)
    }

    private fun handleProjectsResponse(projects: Array<Project?>) {
        Log.d(TAG, "successful project list" + projects.toString())
        (this.projects as MutableLiveData<Array<Project?>>).value = projects
    }

    private fun handleProjectsError(error: Throwable) {
        Log.d(TAG, "error msg--->" + error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.projects as MutableLiveData<Array<Project?>>).value = invalidProjects
        }
    }
}
//    private fun handleProjectsError(error: Throwable) {
//    Log.d(TAG, "error------> "+ error.localizedMessage)
//    if (error is HttpException) {
//        if (error.code() >= 401) {
//            var msg = error.response()?.errorBody()?.string()
//            var message = JsonParser().parse(msg)
//                    .asJsonObject["message"]
//                    .asString
//            if (message != null && message.isNotEmpty()) {
//                errorMessage = message + " Please enter valid barcode."
//            } else {
//                errorMessage = error.message()
//            }
//        }
//        (networkError as MutableLiveData<Boolean>).value = true
//    } else if (error is SocketException || error is SocketTimeoutException) {
//        (networkError as MutableLiveData<Boolean>).value = true
//    } else {
////            (this.user as MutableLiveData<PopulatedUser>).value = invalidUser
//    }
//}
//}
