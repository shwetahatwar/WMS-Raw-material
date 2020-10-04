package com.briot.wms.implementor.ui.main

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.PutawayLocationScan
import com.briot.wms.implementor.repository.remote.RemoteRepository

class PutawayLocationScanViewModel : ViewModel() {

    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val invalidPutawayLocItems: Array<PutawayLocationScan?> = arrayOf(null)
    val putawayLocationScan: LiveData<Array<PutawayLocationScan?>> = MutableLiveData()
    var locationBarcode: String? = ""
    var name:String? = null

    fun loadPutawayLocationScanItems(barcodeSerial: String) {
        // (this.putawayLocationScan as MutableLiveData<Array<PutawayLocationScan?>>).value = emptyArray()
        RemoteRepository.singleInstance.getPutawayLocationScan(barcodeSerial, this::handlePutawayLocationScanResponse,
                this::handlePutawayLocationScanError)
    }

    private fun handlePutawayLocationScanResponse(putawayLocation: Array<PutawayLocationScan?>) {
        println("putawayLocation - >"+putawayLocation)
        for (i in putawayLocation){
            println("item - >"+i?.barcodeSerial)
        }
        var locationFound = putawayLocation.filter{it!!.barcodeSerial.toString() == locationBarcode.toString()}
        if (locationFound.size > 0){
            (this.putawayLocationScan as MutableLiveData<Array<PutawayLocationScan?>>).value = putawayLocation
        } else {
            (this.putawayLocationScan as MutableLiveData<Array<PutawayLocationScan?>>).value = emptyArray()
        }
    }

    private fun handlePutawayLocationScanError(error: Throwable) {
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.putawayLocationScan as MutableLiveData<Array<PutawayLocationScan?>>).value = invalidPutawayLocItems

//            (this.putawayItems as MutableLiveData<Array<PutawayItems?>>).value = invalidPutawayItems
        }
        Log.d(TAG, "error -->"+error.localizedMessage)
    }
    //End of putaway location scan API
}