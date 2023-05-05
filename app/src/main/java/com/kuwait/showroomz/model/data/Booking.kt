package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.model.local.LocalRepo
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
open class Booking(
    @PrimaryKey open var id: String? = "",

    @SerializedName("modelData") open var model: String? = "",
    open var discr: String? = "",
    @SerializedName("client") open var clientId: String = "",
    open var amountPaid: String? = "",
    @SerializedName("program") open var programId: String? = "",
    open var price: String? = "",
    open var createdAt: String? = "",
    open var updatedAt: String? = "",
    open var startBookingDate: String? = "",
    open var startBookingTime: String? = "",
    open var endBookingDate: String? = "",
    open var endBookingTime: String? = "",
    @SerializedName("deliveryAddress") open var deliveryAddressId: String? = "",
    @SerializedName("dropoutAddress") open var dropoutAddressId: String? = null,
    open var numberOfDays: Int? = 0,
    open var rentType: Int? = 0,
    open var paymentMethod: Int? = 0,
    open var addons: @RawValue RealmList<Addon>? = RealmList(),
    open var status: Int? = 0,
    open var civilIdFile:Image?=null,
    open var driversLicense:Image?=null, open var isEnabled:Boolean?=true,
    open var offers:@RawValue RealmList<Offer>? = RealmList()
) : RealmObject(), Parcelable

fun Booking.client():User?{
    val local = LocalRepo()
    return local.getOne(clientId)
}

fun Booking.modelData(): ModelBasic?{
    val local = LocalRepo()
    return model?.let { local.getOne(it) }
}

fun Booking.program(): Program?{
    val local = LocalRepo()
    return programId?.let { local.getOne(it) }
}

fun Booking.deliveryAddress(): UserAddress?{
    val local = LocalRepo()
    return deliveryAddressId?.let { local.getOne(it) }
}

fun Booking.dropoutAddress(): UserAddress?{
    val local = LocalRepo()
    return dropoutAddressId?.let { local.getOne(it) }
}