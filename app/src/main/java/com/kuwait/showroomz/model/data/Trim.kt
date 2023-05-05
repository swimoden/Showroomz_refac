package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable
@Parcelize
open class Trim(
    @PrimaryKey open var id: String = "",
    open var price: Int = 0,
    open var modelId: Int = 0,
    open var colors: @RawValue RealmList<Color> = RealmList<Color>(),
    open var programs:@RawValue RealmList<Program> = RealmList<Program>(),
    open var specs:@RawValue RealmList<Spec> = RealmList<Spec>(),
    open var services:@RawValue RealmList<Service> = RealmList<Service>(),
    open var translations: Translation? = null,
    open var isEnabled:Boolean?=true,
    open var position: Int = -1

) : RealmObject(), Serializable, Parcelable
@Parcelize
open class Color(
    open var top: String = "",   open var isEnabled:Boolean?=true,
    open var bottom: String = ""
) : RealmObject(), Serializable, Parcelable
@Parcelize
open class Program(
    open var contractPeriod: Int = 0,
    open var monthlyPayment: Double = 0.0,
    open var isEnabled:Boolean?=true,
    open var mileageLimit: String? = "",
    open var id: String = ""
) : RealmObject(), Serializable, Parcelable
@Parcelize
open class Spec(
    @PrimaryKey open var id: String = "",
    open var type: String = "",   open var isEnabled:Boolean?=true,
    @SerializedName("contents")
    open var contents: @RawValue RealmList<SpecContent> = RealmList<SpecContent>(),
    open var translations: Translation? = null,
    open var position: Int = -1

) : RealmObject(), Serializable,Parcelable
@Parcelize
open class SpecContent(
    @PrimaryKey open var id: String = "",
    open var translations: Translation? = null,
    open var isChoice: Boolean? = false,   open var isEnabled:Boolean?=true,
    open var isChecked:Boolean?=false,
    open var isMultiple:Boolean?=false
) : RealmObject(), Serializable,Parcelable
@Parcelize
open class Service(
    open var icon: Image? = null,   open var isEnabled:Boolean?=true,
    open var translations: Translation? = null
) : RealmObject(), Serializable,Parcelable
@Parcelize
open class Offer(
    @PrimaryKey open var id: String = "",
    open var modelId:Int = -1,
    open var icon: Image? = null,
    open var color: String? = "",   open var isEnabled:Boolean?=true,
    open var discountValue: String? = "",
    open var type: Int? = -1,
    open var banks:@RawValue RealmList<Bank> = RealmList<Bank> (),
    open var modelDataIds:@RawValue RealmList<Int> = RealmList<Int> (),
    open var isInCorporation: Boolean? = false,
    open var translations: Translation? = null,
    open var position: Int = -1

) : RealmObject(), Serializable,Parcelable,DataObject
@Parcelize
open class TrimResponse(
    @PrimaryKey open var id: String = "",   open var isEnabled:Boolean?=true,
    @SerializedName("trims")
    open var trims:@RawValue RealmList<Trim>? = RealmList(),
    open var offers:@RawValue RealmList<Offer>? = RealmList(),
    open var position: Int = -1


) : RealmObject(), Serializable,Parcelable