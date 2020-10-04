package com.briot.wms.implementor.ui.main

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.ProjectList
import com.briot.wms.implementor.repository.remote.RemoteRepository
import java.util.ArrayList

class ProjectViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val networkError: LiveData<Boolean> = MutableLiveData()
    val projectList: LiveData<Array<ProjectList?>> = MutableLiveData()
    val invalidProjects: Array<ProjectList?> = arrayOf(null)
    var ProjectCount: Number? = 0
    var projectListRes = ArrayList<ProjectList>()
    var errorMessage: String?= ""


    fun loadProjectListData(){
        RemoteRepository.singleInstance.getProjectList(
                this::handleProjectResponse, this::handleProjectError)
    }

    private fun handleProjectResponse(projects: Array<ProjectList?>) {
        for (i in projects){
            if (i != null) {
                projectListRes.add(i)
            }
        }
        (this.projectList as MutableLiveData<Array<ProjectList?>>).value = projects
    }


    private fun handleProjectError(error: Throwable) {
        Log.d(ContentValues.TAG, "error msg--->"+error.localizedMessage)

        if (UiHelper.isNetworkError(error)) {
            errorMessage = error.message
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.projectList as MutableLiveData<Array<ProjectList?>>).value = invalidProjects
        }
    }
}
