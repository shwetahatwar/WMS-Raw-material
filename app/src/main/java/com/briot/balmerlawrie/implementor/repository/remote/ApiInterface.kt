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

class MaterialDetails {
    var id: Number? = null
    var createdAt: Number? = null
    var updatedAt: Number? = null
    var requestedQuantity: Number? = null
    var actualQuantity: Number? = null
    var materialStatus: String? = null
    var status: Number? = null
    var estimatedDate: String? = null
    var barcodeSerial: String? = null
    var createdBy: User? = null
    var updatedBy: User? = null
}


interface ApiInterface {
    @POST("users/sign_in")
    fun login(@Body signInRequest: SignInRequest): Observable<SignInResponse>

    @GET("material")
    fun getMaterialDetail(@Query("material") barcodeSerial: String): Observable<Array<MaterialDetails>>

}
