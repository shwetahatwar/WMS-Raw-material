package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.data.AppDatabase
import com.briot.balmerlawrie.implementor.data.PutawayScanList
import com.briot.balmerlawrie.implementor.repository.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class PutawayViewModel : ViewModel() {
    var description: String? = null
    var barcodeSerial: String? = null
    var partnumber: Number? = null
    var quantity: Number? = null
    var pendingForPutaway: Number? = null
    var shelf:String? = null
    var shelfId: String? = null
    var name:String? = null
    var location_value:String? = null
    var errorMessage: String = ""

    val totalScannedItem: LiveData<Number> = MutableLiveData()
    val networkError: LiveData<Boolean> = MutableLiveData()
    val itemSubmissionSuccessful: LiveData<Boolean> = MutableLiveData()
    val pendingItem: LiveData<Boolean> = MutableLiveData()
    val invalidMaterial: LiveData<Boolean> = MutableLiveData()

    val putawayMaterialScanData: LiveData<Array<MaterialInward?>> = MutableLiveData()

    val putawayList = ArrayList<Putaway>()
    val putawayDashboardData: LiveData<PutawayDashboardData?> = MutableLiveData()
    val invalidPutawayDashboardData: Array<PutawayDashboardData?> = arrayOf(null)
    var materialInwardsPutaway: LiveData<Array<MaterialInwards?>> = MutableLiveData()

    private var appDatabase = AppDatabase.getDatabase(MainApplication.applicationContext())


    fun loadPutawayMaterialScan(barcodeSerial: String){
        (networkError as MutableLiveData<Boolean>).value = false
        RemoteRepository.singleInstance.getPutawayMaterialScan(barcodeSerial,
                this::handlePutawayMaterialScanResponse,
                this::handlePutawayMaterialScanError)
    }

    private fun handlePutawayMaterialScanResponse(res: Array<MaterialInward?>) {
//        var thisobj = this
////        (thisobj.putawayMaterialScanData as MutableLiveData<Array<MaterialInward?>>).value = putawayMaterialScan
        if (res.size != 0){
            if (res[0]?.QCStatus == 1){
                putMaterialScantems()
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        addPutawayMaterial(res[0])
                    }
                }
            }else{
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        (pendingItem as MutableLiveData<Boolean>).value = true
                    }
                }
            }
        }else {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    (invalidMaterial as MutableLiveData<Boolean>).value = true
                }
            }
        }
    }

    private fun handlePutawayMaterialScanError(error: Throwable) {
//        Log.d(TAG,"error->"+error.localizedMessage)
//        Log.d(ContentValues.TAG,"error->"+error.localizedMessage)
//        Log.d(ContentValues.TAG, "error msg--->"+error.localizedMessage)
        if (UiHelper.isNetworkError(error)) {
            println("------inside network error----")
            errorMessage = error.message.toString()
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            errorMessage = error.message.toString()
//            (this.violatedData as MutableLiveData<Array<MaterialInward?>>).value = res
        }
    }


    fun putMaterialScantems() {
        var requestbody = putMaterialItem()
        requestbody.materialStatus = "Available"
        requestbody.shelfId = shelfId?.toInt()
        // println("requestbody-->"+requestbody)
        RemoteRepository.singleInstance.putMaterialScantems(barcodeSerial, requestbody,
                this::handlePutPutawayMaterialScanResponse,
                this::handlePutPutawayMaterialScanError)
    }

    private fun handlePutPutawayMaterialScanResponse(putResponse: DispatchSlipItemResponse?) {
        println("updated successfully")
        // println("after delete call putResponse-->"+ putResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                (itemSubmissionSuccessful as MutableLiveData<Boolean>).value = true
            }
        }
        // pendingForPutaway = pendingForPutaways!!.responseData.pendingForPutaway
    }

    private fun handlePutPutawayMaterialScanError(error: Throwable) {
        Log.d(ContentValues.TAG, "-----in error"+ error)
        if (UiHelper.isNetworkError(error)) {
            (networkError as MutableLiveData<Boolean>).value = true
        } else {
            (this.putawayDashboardData as MutableLiveData<Array<PutawayDashboardData?>>).value = invalidPutawayDashboardData
        }
    }
    //end of putaway total count

    //start room database
    suspend fun addPutawayMaterial(matQcStatus: MaterialInward?) {
        // Log.d(TAG,"addPutawayMaterial -->"+barcodeSerial)
        val putawayScanObj = MaterialInward()
        putawayScanObj.barcodeSerial = matQcStatus?.barcodeSerial
        putawayScanObj.partnumber.partNumber = matQcStatus?.partnumber?.partNumber
        putawayScanObj.partnumber.description = matQcStatus?.partnumber?.description
        putawayScanObj.eachPackQuantity = matQcStatus?.eachPackQuantity
        updateItemInDatabase(putawayScanObj)
    }

    private suspend fun updateItemInDatabase(item: MaterialInward) {
        var dbItem = PutawayScanList(
                id = null,
                barcodeSerial = item.barcodeSerial,
                partnumber = item.partnumber.partNumber,
                description = item.partnumber.description,
                eachPackQuantity = item.eachPackQuantity,
                timeStamp = Date().time)
        var dbDao = appDatabase.putawayScanListDao()
        try {
            dbDao.insert(item = dbItem)
        }
        catch (e: Exception){
            Log.d(TAG, "Getting exception while inserting data to db "+ e)
        }
        try {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    getItemsFromDB()
                }
            }
        }  catch (e: Exception){
            Log.d(TAG, "--inside globallaunch exception "+ e)
        }
    }

    fun getItemsFromDB() {
        var dbDao = appDatabase.putawayScanListDao()
        var dbItems = dbDao.getAllItems()
        (this.totalScannedItem as MutableLiveData<Number>).value = dbItems.size
        dbItems.sortByDescending { it?.timestamp}
        (this.materialInwardsPutaway as MutableLiveData<Array<MaterialInwards?>>).value = dbItems
    }

    fun deleteSelectedAuditFromDB(barcodeSerial: String?) {

        var dbDao = appDatabase.putawayScanListDao()
        GlobalScope.launch {
            if (barcodeSerial != null) {
                dbDao.deleteSelectedPutawayFromDB(barcodeSerial)
            }
            withContext(Dispatchers.Main) {
                //updatedListAsPerDatabase()
            }
        }
    }

    fun deletePutawayScanItemsFromDB() {
        var dbDao = appDatabase.putawayScanListDao()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                dbDao.deleteAll()
                getItemsFromDB()
            }
        }
    }
}