package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Setting (
    @SerializedName("id")
    @PrimaryKey open var id: String = "",
    @SerializedName("IOSVersion")  open var iosVersion: String? = "",
    @SerializedName("AndroidVersion") open var androidVersion: String? = "",
    @SerializedName("ForceUpdate")open var forceUpdate: Boolean? = false,
    @SerializedName("FinanceModule")open var financeModule: Boolean? = false,
    @SerializedName("OptionalUpdate")open var optionalUpdate: Boolean? = false,
    @SerializedName("AdvertisementModule") open var advertisementModule: Boolean? = false,   open var isEnabled:Boolean?=true,
    @SerializedName("AdsTimeInterval") open var adsTimeInterval: Int? = 0,
    @SerializedName("FrequencyMan") open var frequencyMan: Int? = 0,
    @SerializedName("FrequencyWomen") open var frequencyWomen: Int? = 0,
    @SerializedName("PhoneNumber") open var phoneNumber:String? = "",
    @SerializedName("RemoteDataVersion") open var remoteDataVersion: Int? = 0

):RealmObject(),Parcelable