package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookNowRequest(
    var paymentType:Int,
    var amountPaid: String,
    var paymentMethod: Int,
    var status: Int,
    var client: String,
    var program: String?=null,
    var modelData: String,
    var civilIdFile: String
) : Parcelable

@Parcelize
data class BookNowRentRequest(
    var paymentType:Int,
    var startBookingDate: String,
    var startBookingTime: String,
    var endBookingDate: String,
    var endBookingTime: String,
    var deliveryAddress: String?,
    var dropoutAddress: String?,
    var numberOfDays: Int,
    var rentType: Int,
    var amountPaid: String,
    var paymentMethod: Int,
    var status: Int,
    var client: String,
    var modelData: String,
    var civilIdFile: String,
    var driversLicense: String,
    var addons: ArrayList<String> = arrayListOf()
) : Parcelable