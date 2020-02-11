package com.briot.balmerlawrie.implementor.repository.remote

import io.reactivex.Observable
import retrofit2.http.*


class SignInRequest {
    var username: String? = null
    var password: String? = null
}

class SignInResponse {
    var message: String? = null
    var token: String? =  null
    var username: String? = null
}

class Role {
    var id: Number? = null
    var roleName:  String? = null
}

class User {
    var username: String? = null
    var id: Number? = null
    var token: String? = null
}

class Material {
    var materialType: String? = null
    var materialCode: String? = null
    var materialDescription: String? = null
    var genericName: String? = null
    var packingType: String? = null
    var packSize: String? = null
    var netWeight: String? = null
    var grossWeight: String? = null
    var tareWeight: String? = null
    var uom: String? = null
    var batchCode: String?  = null
    var status: String? = null
    var createdBy: User? = null
    var updatedBy: User? = null
}

class  MaterialInward {
    var materialId: Material? = null
//    var materialCode: Number = 0
    var batchNumber: String? = null
    var serialNumber: String? = null
    var isScrapped: Boolean = false
    var dispatchSlipId: Number? = null
    var status: Boolean = false
//    var createdBy: User? = null
//    var updatedBy: User? = null
}

class Ttat {
    var truckNumber: String = ""
    var capacity: String = ""
    var inTime: String = ""
    var outTime: String = ""
    var driver: String = ""
    var loadStartTime: String = ""
    var loadEndTime: String = ""
    var loadingTime: String = ""
    var inOutTime: String = ""
    var idleTime: String = ""
//    var createdBy: User? = null
//    var updatedBy: User? = null
}

class Depo {
    var name: String  = ""
    var location: String = ""
    var status: String  = ""
//    var createdBy: User? = null
//    var updatedBy: User? = null
}


class DispatchSlip {
    var dispatchSlipNumber: String = ""
    var truckId: Ttat? = null
    var depoId: Depo? = null
    var status: String = ""
}

class DispatchSlipItem {

}

class Project {

}

class ProjectAuditItem {

}


interface ApiInterface {
    @POST("users/sign_in")
    fun login(@Body signInRequest: SignInRequest): Observable<SignInResponse>

    @GET("material")
    fun getMaterialDetails(@Query("barcode") barcodeSerial: String): Observable<Array<Material>>

    @GET("/dispatchpickerrelation/users/{userid}/dispatchslips/")
    fun getAssignedPickerDispatchSlips(@Path("userid") userId: Int): Observable<Array<DispatchSlip?>>

    @GET("/dispatchloaderrelation/users/{userid}/dispatchslips/")
    fun getAssignedLoaderDispatchSlips(@Path("userid") userId: Int): Observable<Array<DispatchSlip?>>
}
