package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.android.gms.common.api.Api
import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.view.adapters.USerCarDetailsAdapter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable

data class ClientVehicleRequest(
    var imageGallery:ArrayList<String> = arrayListOf() ,
    var modelData:String?="",
    var dealerData:String?="",
    var client:String?="",
    var year:String?="",
    var engine:String?="",
    var cylinder:String?="",
    var mileage:String?="",
    var condition:String?="",
    var description:String?=""
):Serializable
@RealmClass
@Parcelize
open class ClientVehicle(
    open var imageGallery:@RawValue RealmList<Image> = RealmList(),
    open var options:@RawValue RealmList<CarOption> = RealmList(),
    //open var modelData:ModelResponse?= ModelResponse(),
    open var dealerName:String?="",
    open var modelName:String?="",
    open var year:String?="",
    open var engine:String?="",
    open var cylinder:String?="",
    open var mileage:String?="",
    open var condition:String?="",
    open var description:String?="",
    open var clientId:String?="",
    @PrimaryKey
    var id:String?="",
    var createdAt:String?=""
):Parcelable,RealmObject()
fun ClientVehicle.client(): User?{
    val local = LocalRepo()
    return clientId?.let { local.getOne(it) }
}
@Parcelize
open class ClientVehicleBasic(

    @PrimaryKey
    var id:String?=""

):Parcelable,RealmObject()
data class CallbackAppraisalClientVehicleRequest(
    var phone: Phone,
    var email:String,
    var fullName:String,
    var modelData:String?,
    var clientVehicleId:String,
    var clientId:String
):Serializable

 class CallbackAppraisalClientVehicleRequestWithoutModel(
    var phone: Phone,
    var email:String,
    var fullName:String,
    var clientVehicleId:String,
    var clientId:String
):Serializable
@Parcelize
open class CallbackAppraisalClientVehicle(
    open var discr: String?="",
    open var clientVehicle: ClientVehicleBasic? = null,
    open var modelData:ModelResponse?= null,
    open var email:String?="",
    open var requests:@RawValue RealmList<AppraisalRequest>?= RealmList(),
    open var client:User?= null,
    open var fullName:String?="",
    open var comment:String?="",
    open var civilId:String?="",
    open var status:String?="",
    @PrimaryKey
    open var id:String?="",
    open var createdAt:String?="",
    open var isFromMasterData: Boolean?=false,
    open var clientId:String?="",
    open var clientVehicleId:String?="",

):Parcelable,RealmObject()


@Parcelize
    open class AppraisalRequest(
    open var appraisal:AppraisalData?= null,
    open var status:String?="",
    open var price:String?="",
    @PrimaryKey open var id:String?="",
    open var createdAt:String?="",
    open var isEnabled: Boolean?=true,
    @Ignore var isSelected:Boolean=false
):Parcelable,RealmObject()
@Parcelize
open class AppraisalData(
    open var logo:Image?=Image(),
    open var brand:Brand?= Brand(),
    open var username:String?="",
    open var email:String?="",
   // open var address:String?=null,
    open var translations:Translation?= Translation(),
    open var phone:Phone?= Phone(),
    open var price:String?="",
    open var position:Int?=-1,
    @PrimaryKey open var id:String?="",
    open var createdAt:String?="",
    open var isEnabled: Boolean?=true
):Parcelable,RealmObject()
@Parcelize
open class AppraisalResp(
    @PrimaryKey open var id:String?=""
):Parcelable

data class CallbackAppraisalClientVehicleResponse(
    var status:String?="",
    var message:String?="",
    @SerializedName ("Result") var data: CallbackAppraisalClientVehicle?= CallbackAppraisalClientVehicle()
):Serializable

data class DataCallbackAppraisal(
    var callback: CallbackAppraisalClientVehicle?= CallbackAppraisalClientVehicle(),
    var token:String?=""
):Serializable

data class ClientVehicleResponse(
    var status:String?="",
    var message:String?="",
    @SerializedName("Result") var data: ClientVehicle?= null
):Serializable

data class DataClientVehicle(
    var clientVehicle: ClientVehicle? = null ,
    var token:String?=""
):Serializable

@Parcelize
open class AppraisalInfo(
    @PrimaryKey
   open var id:String?="",
    open var position:Int = -1,
   open var translations:Translation?=null,
   open var appraisalVehicleBrandModels:@RawValue RealmList<AppraisalInfo> = RealmList()

):Serializable, RealmObject(),Parcelable {
    var lang = if (isEnglish) translations?.en else translations?.ar
    var name = lang?.let {
        it.name
    } ?: run {
        ""
    }
}