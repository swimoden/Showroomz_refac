package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BuyNowRequest(
    var paymentType:Int,
    var price: String,
    var amountPaid: String,
    var paymentMethod: Int,
    var status: Int,
    var client: String,
    var modelData: String,
    var civilIdFile: String,
    var driversLicense: String,
    var offers: ArrayList<String> = arrayListOf()
) : Parcelable

@Parcelize
data class PaymentResponse(var status: String, var paymentURL: String) : Parcelable