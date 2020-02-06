package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import android.util.Log
import com.briot.balmerlawrie.implementor.repository.remote.MaterialDetails
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
import java.net.SocketException
import java.net.SocketTimeoutException

class MaterialDetailsScanViewModel : ViewModel() {
    val TAG = "MaterialScanViewModel"

    val materialDetails: LiveData<MaterialDetails> = MutableLiveData<MaterialDetails>()

    val networkError: LiveData<Boolean> = MutableLiveData<Boolean>()
    val invalidMaterialDetail: MaterialDetails = MaterialDetails()

    fun loadMaterialDetails(barcodeSerial: String) {
        (networkError as MutableLiveData<Boolean>).value = false
        RemoteRepository.singleInstance.getMaterialDetails(barcodeSerial, this::handleMaterialResponse, this::handleMaterialError)
    }

    private fun handleMaterialResponse(materialDetails: Array<MaterialDetails>) {
        Log.d(TAG, "successful material" + materialDetails.toString())
        if (materialDetails.size > 0) {
            (this.materialDetails as MutableLiveData<MaterialDetails>).value = materialDetails.first()
        }else {
            (this.materialDetails as MutableLiveData<MaterialDetails>).value = null

        }
    }

    private fun handleMaterialError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (error is SocketException || error is SocketTimeoutException) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.materialDetails as MutableLiveData<MaterialDetails>).value = null
        }
    }
}
