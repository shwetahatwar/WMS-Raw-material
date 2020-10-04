package com.briot.wms.implementor.repository.remote

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

class SignInRequest {
    var username: String? = null
    var password: String? = null
}

class SignInResponse {
    var message: String? = null
    var token: String? =  null
    var username: String? = null
    var userId: Number? = null
    var employeeId: String? = null
    var roleId: Number? = null
    var role: String? = null
}


class userResponse {
    var username: String? = null
    var roleId: Number? = null
    var role: String? = null
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
    var UOM: String? = null
    var batchCode: String?  = null
    var status: String? = null
//    var createdBy: User? = null
//    var updatedBy: User? = null
}

class qcStatusDisplay {
    var id: Int? = null
    var qcId: Int? = null
    var barcodeSerial: String? = null
    var description: String? = null
    var partnumber: String? = null
    var location: String? = null
    var QCStatus: String? = null
}

class MaterialInward(issueReturn: Boolean?=false) {
    var id: Int? = null
    var barcodeSerial: String? = null
    var partnumber: partnumber = partnumber()
    var shelf: shelf = shelf()
    var QCStatus: Int? = null
    var eachPackQuantity: Int? = null
    var batchNumber: String? = null
    }

class MaterialInwards{
    var id: Int? = null
    var barcodeSerial: String? = null
    var shelf: String? = null
    var QCStatus: Int? = null
    var partnumber: String? = null
    var description: String? = null
    var eachPackQuantity: Int? = null
    var partNumber: String? = null
    var batchNumber: String? = null
    var timestamp: Long? = null
}

class shelf{
    var name: String? = null
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
    var createdBy: String? = null
    var updatedBy: String? = null
    var createdAt: String? = null
    var updatdAt: String? = null
}

class Depo {
    var name: String  = ""
    var location: String = ""
    var status: String  = ""
    var createdBy: String? = null
    var updatedBy: String? = null
    var createdAt: String? = null
    var updatdAt: String? = null
}


class DispatchSlip {
    var id: Number? = null
    var dispatchSlipNumber: String = ""
    var truckId: Number? = null
    var depoId: Number? = null
    var status: String = ""
    var ttat: Ttat? = null
    var dispatchSlipStatus: String? = null
    var depot: Depo? = null
    var createdBy: String? = null
    var updatedBy: String? = null
    var createdAt: String? = null
    var updatdAt: String? = null
}

class DispatchSlipItem {
    var id: Number? = null
    var dispatchSlipId: Number? = null
    var batchNumber: String? = null
    var numberOfPacks: Number = 0
    var materialCode: String? = null
    var materialGenericName: String? = null
    var materialDescription: String? = null
    var createdBy: String? = null
    var updatedBy: String? = null
    var createdAt: String? = null
    var updatdAt: String? = null
    var scannedPacks: Number = 0
}

class DispatchSlipItemRequest {
    var batchNumber: String? = null
    var serialNumber: String? = null
    var materialCode: String? = null
}

class DispatchSlipItemResponse {
    var message: String? = null
}

class DispatchSlipRequest {
    var loadStartTime: Number? = null
    var loadEndTime: Number? = null
    var truckNumber: String? = null
    var dispatchId: Number? = null
    var truckId: Number?  = null
    var materials: Array<DispatchSlipItemRequest>? = null
}

class Project {
    var id: Int = 0
    var name: String = ""
    var auditors: String = ""
    var start: String = ""
    var end: String = ""
    var status: Boolean = false
    var projectStatus: String? = null
    var createdBy: String? = null
    var updatedBy: String? = null
    var createdAt: String? = null
    var updatdAt: String? = null
}

class picklistMasterRes {
    var picklistName: String? = null
    var picklistStatus: String? = null
    var name: String? = null
    var createdAt: String? = null
    var id: Int? = null
}

class picklistMasterTotalCount {
    var total: String? = null
    var inProgress: String? = null
    var completed: String? = null
}




class auditProjectItem {
    var projectId: Number? = null
    var serialNumber: String? = null
}

class auditProjectList {
    var projectId: Int = 0
    var serialNumber: String? = null
    var batchCode : String? = null
    var productCode: String? = null
}

class responseData  {
    var totalData: Number? = null
    var pendingForPutaway: Number? = null
}
class PutawayDashboardData{
    var responseData: responseData = responseData()
}

class PickingDashboardData{
    var inProgress: Number? = null
    var pending: Number? = null
    var completed: Number? = null
    var total: Number? = null
}

class PickingMasterDisplay{
    var quantityPicked : String? = null
    var partNumber: String? = null
    var partDescription: String? = null
    var numberOfPacks: Int? = null
    var barcodeSerial: String? = null
    var batchNumber: String? = null
    var location: String? = null
    var pickedBarcode: String? = null
    var purchaseOrderNumber: String? = null
    var id: Int? =null
}



class ProjectItem {
    var projectId: Number? = null
    var materialCode: String? = null
    var batchNumber: String? = null
    var serialNumber: String? = null
    var itemStatus: String? = null
}
class QCPending{
    var id: Number? = null
    var QCStatus: Number? = null
    var prevQCStatus: Number? = null
    var barcodeSerial: Number? = null
    var partnumber: partnumber = partnumber()
}

class partnumber{
    var description: String? = null
    var partNumber: String? = null
}

class QCTotalCount{
    var QCStatus: QCStatus = QCStatus()
}

class QCStatus{
    var ok: Number? = null
    var pending: Number? = null
    var rejected: Number? = null
    var total: Number? = null
}

class QCScanItem{
    var id: Int? = null
    var qcId: Int? = null
    var barcodeSerial: String? = null
    var QCStatus: Int? = null
    var prevQCStatus: Int? = null
    var QCRemarks: String? = null
    var timestamp: Long? = null
}

class PicklistMaster {
    var batchNumber: String? = null
    var isViolated: Boolean? = null
    var partNumber: String? = null
    var purchaseOrderNumber: String? = null
    var quantity: Int? = null
    var serialNumber: String? = null
    var violatedSerialNumber: String? = null
}

class Materials {
    var batchNumber: String? = null
    var isViolated: Boolean? = null
    var partNumber: String? = null
    var purchaseOrderNumber: String? = null
    var quantity: Int? = null
    var serialNumber: String? = null
    var violatedSerialNumber: String? = null
    var timestamp: Long? = null
}

class PostPickingData {
    var picklistId: Int? = null
    var userId: Int? = null
    var completedPicklistId: Int? = null
    var materials: Array<Materials>? = null
}

class Putaway{
    var partnumber: partnumber = partnumber()
    var shelf: shelf = shelf()
    var eachPackQuantity: Int? = null
    var barcodeSerial: String? = null
}
class PutawayLocationScan{
    var barcodeSerial: String? = null
    var name: String? = null
    var id: Int? = null
}

class putMaterialItem {
    var materialStatus: String? = null
    var shelfId: Int? = null
}

class picklistPostMaterials{
    var quantityPicked : String? = null
    var partNumber: String? = null
    var partDescription: String? = null
    var numberOfPacks: String? = null
    var barcodeSerial: String? = null
    var batchNumber: String? = null
    var location: String? = null
    var pickedBarcode: String? = null
}

class Employee{
    var employeeId: String? = null
    var username: String? = null
    var id: Int? = null
}

class ProjectList{
    var id: Int? = null
    var name: String? = null
    var description: String? = null
}
class MaterialData{
    var partNumber: String? = null
    var batchNumber: String? = null
    var serialNumber: String? = null
    var quantityPicked: Int? = null
    var id: Int? = null
}

class IssueToProductionList {
    var projectId: Int? = null
    var materialInwardId: Int? = null
    var userId: Int? = null
    var quantity: Int? = null
    var picklistId: Int? = null
    var barcodeSerial: String? = null
}

class IssueToProductionResponse{
    var picklistId: Int? = null
    var id: Int? = null
    var transactionTimestamp: String? = null
    var materialInwardId: Int? = null
    var quantity: Int? = null
    var transactionType: String? = null
    var batchNumber: String? = null
    var serialNumber: String? = null
}

class IssueToReturnMaterialInwards{
    var id: Int? = null
    var barcodeSerial: String? = null
    var shelf: String? = null
    var QCStatus: Int? = null
    var partnumber: String? = null
    var description: String? = null
    var eachPackQuantity: Int? = null
    var partNumber: String? = null
    var batchNumber: String? = null
    var materialInwardId: Int? = null
    var performedBy: Int? = null
    var transactionType: String? = null

}

class doneBy{
    var id: Int? = null
    var employeeId: Int? = null

}

class ReturnFromProduction{
    var remarks: String? = null
    var quantity: Int? = null
    var materialInwardId: Int? = null
    var projectId: Int? = null
    var materialinward: IssueToReturnMaterialInwards = IssueToReturnMaterialInwards()
    var doneBy: doneBy = doneBy()
    var userId: Int? = null
    var transactionType: String? = null
}


class ReturnFromProductionScanItem{
    var materialInwardId: Int? = null
    var quantity: Int? = null
    var projectId: Int? = null
    var remarks: String? = null
    var userId: Int? = null
    var barcodeSerial: String? = null
}

//class materialinward{
//    var barcodeSerial: String? = null
//    var partNumber: String? = null
//    var batchNumber: String? = null
//    var eachPackQuantity: Int? = null
//}
class ReturnFromProductionResponse{
    var id: Int? = null
    var barcodeSerial: String? = null
    var transactionTimestamp: String? = null
    var materialInwardId: Int? = null
    var projectId: Int? = null
    var performedBy: Int? = null
    var transactionType: String? = null
    var remarks: String? = null
    var quantity: Int? = null
    var partNumber: String? = null
    var batchNumber: String? = null
}

interface ApiInterface {
    @POST("users/sign_in")
    fun login(@Body signInRequest: SignInRequest): Observable<SignInResponse>

    @GET("users")
    fun getUsers(): Observable<Array<userResponse?>>

    @GET("materialinwards/get/dashboardCountForPendingPutaway")
    fun getPutawayCount(): Observable<PutawayDashboardData?>

    @GET("materialinwards/get/getCountByQcStatusHHT")
    fun getQcTotalCount(): Observable<QCTotalCount?>

    @GET("materialinwards?QCStatus=0")
    fun getQcPendingCount(@Query("limit") limit: String, @Query("offset" ) offset: String): Observable<Array<QCPending?>>

    @GET("picklists/dashboard/count")
    fun getPickingCount(): Observable<PickingDashboardData?>

    @GET("dispatchslip")
    fun getDispatchSlip(@Path("id") dispatchSlipId: String): Observable<Array<DispatchSlip>>

    @GET("/dispatchpickerrelations/users/{userid}/dispatchslips")
    fun getAssignedPickerDispatchSlips(@Path("userid") userId: Int): Observable<Array<DispatchSlip?>>

    @GET("/dispatchloaderrelations/users/{userid}/dispatchslips")
    fun getAssignedLoaderDispatchSlips(@Path("userid") userId: Int): Observable<Array<DispatchSlip?>>

    @GET("/dispatchslips/{id}/dispatchslipmaterials")
    fun getDispatchSlipMaterials(@Path("id") id: Int): Observable<Array<DispatchSlipItem?>>

    @POST("dispatchslips/{id}/dispatchslippickedmaterials")
    fun postDispatchSlipPickedMaterials(@Path("id") id: Int, @Body requestbody: DispatchSlipRequest): Observable<DispatchSlipItemResponse?>

    @POST("dispatchslips/{id}/dispatchsliploadermaterials")
    fun postDispatchSlipLoadedMaterials(@Path("id") id: Int, @Body requestbody: DispatchSlipRequest): Observable<DispatchSlipItemResponse?>

    @GET("/projects")
    fun getAuditProjects(@Query("projectStatus") projectStatus: String): Observable<Array<Project?>>

    @GET("/project/{id}/projectitems")
    fun getProjectItems(@Path("id") id: String): Observable<Array<ProjectItem?>>

    @POST("/projects/projectItems")
    fun postProjectItems(@Body auditRequestBody: Array<auditProjectItem>): Observable<auditProjectItem?>

    @POST("/materialinwards/post/qcstatuschangehht")
    fun postQcPendingScanItems(@Body qcScanRequestBody: Array<QCScanItem>): Observable<ResponseBody>

    @GET("materialinwards")
    fun getMaterialStatus(@Query("barcodeSerial")  barcodeSerial: String): Observable<Array<MaterialInward?>>

    @GET("shelfs?status=1")
    fun getPutawayList(@Query("barcodeSerial")  barcodeSerial: String): Observable<Array<Putaway?>>

    @GET("shelfs?status=1")
    fun getPutawayLocationScan(@Query("barcodeSerial")  barcodeSerial: String): Observable<Array<PutawayLocationScan?>>

    @GET("materialinwards?status=true")
    fun getPutawayMaterialScan(@Query("barcodeSerial")  barcodeSerial: String): Observable<Array<MaterialInward?>>

    @PUT("materialinwards/updateWithBarcode/{barcodeSerial}")
    fun putMaterialScantems(@Path("barcodeSerial") barcodeSerial: String?,
                            @Body putawayReqBody: putMaterialItem): Observable<DispatchSlipItemResponse?>

    @GET("picklists?status=true&limit=100&picklistStatus=In%20Progress&picklistStatus=Pending&picklistStatus=Completed")
    fun gePickListMaster(@Query("offset")  offset: String): Observable<Array<picklistMasterRes?>>

    @GET("picklists/get/count")
    fun getPickListCount(): Observable<picklistMasterTotalCount?>

    @GET("picklists/{picklistId}/picklistmaterials?limit=100&offset=0")
    fun getCompletedItems(@Path("picklistId") picklistId:String): Observable<Array<PickingMasterDisplay?>>

    @POST("/picklists/{picklistId}/picklistpickedmaterials")
    fun postProjectItems(@Path("picklistId") picklistId:String): Observable<picklistPostMaterials?>

    @GET("picklists?status=true&limit=100&picklistStatus=Completed")
    fun getIssueToProductionMaster(@Query("offset")  offset: String): Observable<Array<picklistMasterRes?>>

    @GET("/users")
    fun getEmployeeScan(@Query("employeeId") employeeId: String): Observable<Array<Employee?>>

    @GET("projects?status=true")
    fun getProjectList(): Observable<Array<ProjectList?>>

//    @POST("picklists/{picklistId}/picklistpickedmaterials")
//    fun postViolatedItem(@Path("picklistId") picklistId: Int, @Body qcScanRequestBody: Array<violatedItem>): Observable<ResponseBody>

    @GET("picklistpickingmateriallists?isMaterialIssuedToProduction=false")
    fun getIssueToProductionMaterial(@Query("picklistId") picklistId: Int?): Observable<Array<MaterialData?>>

    @POST("/issuetoproductiontransactions/post/issuetoproduction")
    fun postIssueToMaterial( @Body putawayReqBody: Array<IssueToProductionList>): Observable<Array<IssueToProductionResponse?>>

    @POST("picklists/{id}/picklistpickedmaterials")
    fun postPickingMasterData(@Path("id") id:String?, @Body putawayReqBody: PostPickingData): Observable<DispatchSlipItemResponse?>

    @GET("/issuetoproductiontransactions")
    fun getReturnFromProduction(@Query("projectId") projectId: Int?,@Query("performedBy") performedBy: Int?): Observable<Array<ReturnFromProduction?>>

    @POST("/issuetoproductiontransactions/post/returnfromproduction")
    fun postReturnIssueToMaterial(@Body putawayReqBody: Array<ReturnFromProductionScanItem?>): Observable<Array<ReturnFromProductionResponse?>>

}
