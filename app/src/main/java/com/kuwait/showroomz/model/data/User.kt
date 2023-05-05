package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class User(
    @PrimaryKey open var id: String? = "",
    open var discr: String? = "",
    open var email: String? = "",
    open var fullName: String? = "",
    open var civilID: String? = "",
    open var dateOfBirth: String? = "",
    open var address: String? = "",
    open var gender: Int? = 0,
    open var status: Int? = 0,
    open var image: Image? = null, open var isEnabled: Boolean? = true,
    open var driversLicense: Image? = null,
    open var phone: Phone? = Phone(), var haveCreditCard: Boolean? = null,
    open var nationality: Int? = -1,
    open var employeType: Int? = -1,
    open var companyName: String? = "",
    open var industry: Industry? = Industry(),
    open var salary: String? = "",
    open var isRetired: Boolean? = null,
    open var monhtlyLoanPayment: String? = ""


) : RealmObject(), Parcelable

data class UserRequest(
    var email: String? = "",
    var fullName: String? = "",
    var civilID: String? = "",
    var dateOfBirth: String? = "",
    var address: String? = "",
    var gender: Int? = 0,
    var status: Int? = 0,
    var image: String? = null,
    var driversLicense: String? = null,
    var phone: Phone? = Phone(),
    var haveCreditCard: Boolean? = null,
    var nationality: Boolean? = null,
    var employeType: Int? = 0,
    var companyName: String? = "",
    var industry: String? = "",
    var salary: String? = "",
    var monhtlyLoanPayment: String? = "",
    var bank: String? = ""

)
