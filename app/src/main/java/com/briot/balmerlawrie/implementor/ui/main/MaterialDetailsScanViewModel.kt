package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import android.util.Log
import com.briot.balmerlawrie.implementor.repository.remote.Material
import com.briot.balmerlawrie.implementor.repository.remote.RemoteRepository
import java.net.SocketException
import java.net.SocketTimeoutException

class MaterialDetailsScanViewModel : ViewModel() {
    val TAG = "MaterialScanViewModel"

    val materials: LiveData<Material> = MutableLiveData<Material>()

    val networkError: LiveData<Boolean> = MutableLiveData<Boolean>()
    val invalidMaterialItem: Material = Material()

    fun loadMaterialItems(barcodeSerial: String) {
        (networkError as MutableLiveData<Boolean>).value = false
        RemoteRepository.singleInstance.getMaterialDetails(barcodeSerial, this::handleMaterialResponse, this::handleMaterialError)
    }

    private fun handleMaterialResponse(materials: Array<Material>) {
        Log.d(TAG, "successful material" + materials.toString())
        if (materials.size > 0) {
            (this.materials as MutableLiveData<Material>).value = materials.first()
        }else {
            (this.materials as MutableLiveData<Material>).value = null

        }
    }

    private fun handleMaterialError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)

        if (error is SocketException || error is SocketTimeoutException) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.materials as MutableLiveData<Material>).value = null
        }
    }
}
