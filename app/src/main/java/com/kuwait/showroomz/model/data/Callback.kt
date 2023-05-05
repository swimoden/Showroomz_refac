package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Callback (
    @PrimaryKey open var id: String? = "",
    open var client: User? = User(),
    open var fullName: String? = "",
    open var modelData: ModelBasic? = null,
    open var comment: String? = "",
    open var status: Int? = 0,
    open var location: Location?=null,
    open var email: String? = "",
    open var phone: Phone? = Phone(),
    open var fromMasterData: Boolean? = false,
    open var masterId: Int? = 0,
    open var civilID: String? = "",
    open var bank: BankResponse? = null,
    open var discr: String? = "",
    open var totalCost: String? = "",
    open var downpayment: String? = "",
    open var loanAmount: String? = "",
    open var installmentPeriod: String? = "",
    open var installmentAmount: String? = "",
    open var profit :String?="",
    open var createdAt:String?="",
    open var bankAccountNumber:String?=null,
    open var processStatus:Int?=-1,
    open var salaryCertificate:Image?=null,   open var isEnabled:Boolean?=true,
    open var akamathopia:Image?=null,
    open var monthsBankStatement:Image?=null,
    open var isPrivate:Boolean?=false

):RealmObject(),Parcelable
@Parcelize
open class UserResponse(
    @PrimaryKey open var id: String? = ""
):RealmObject(),Parcelable
@Parcelize
open class ModelResponse(
    @PrimaryKey open var id: String? = ""
):RealmObject(),Parcelable
@Parcelize
open class BankResponse(
    @PrimaryKey open var id: String? = ""
):RealmObject(),Parcelable