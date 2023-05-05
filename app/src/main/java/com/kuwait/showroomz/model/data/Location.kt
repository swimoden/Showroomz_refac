package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable
@Parcelize
open class Location (
    @PrimaryKey open var id:String = "",
    open var dealerData:Brand? = null,
    open var brandId:Int = -1,
    @SerializedName("typeId") open var type:Int = -1,
    open var status:Int = -1,
    open var address:Address? = null, open var isEnabled:Boolean?=true,
    open var workingHours:@RawValue RealmList<WorkingHours>? = null,
    open var translations:Translation? = null
): RealmObject(), Serializable,Parcelable