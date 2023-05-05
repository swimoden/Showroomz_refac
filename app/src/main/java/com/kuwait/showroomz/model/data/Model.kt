package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.view.listener.RealmListParceler
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.android.parcel.WriteWith
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
open class Model(
    @PrimaryKey open var id: String = "",
    open var brandData: Brand? = null,
    open var dealerData: BrandBasic? = null,
    open var model: ModelStockBasic? = null,
    open var years: @RawValue RealmList<String> = RealmList<String>(),
    open var translations: Translation? = null,
    @SerializedName(value = "price") open var priceDouble: Double? = 0.0,
    open var hasAllOffer: Boolean = false,
    open var hasGallery: Boolean = false,
    open var hasVideo: Boolean = false,
    open var hasExclusiveOffer: Boolean = false,
    open var isPriceOffer: Boolean = false,

    open var isInternalImageUpload: Boolean = false,
    @SerializedName(value = "interiorImage") open var internalImageUpload: String? = null,
    open var internalImageLink: String? = "",
    open var bookingAmount: String? = "",
    open var isExternalImageUpload: Boolean = false,
    open var externalImageLink: String? = "",
    open var externalImageUpload: String? = null,
    open var customImage: Image? = null,
    open var position: Int = -1,
    open var nbViews: Int = -1,
    open var isExclusive: Boolean = false,
    open var isOffer: Boolean = false,
    open var testDriveStatus: Int = -1,
    open var status: Int = -1,
    open var isNew: Boolean? = false,
    open var showActions: Boolean? = false,
    open var isComingSoon: Boolean? = false,
    open var isEnabled: Boolean? = true,
    open var priceFloat: Double? = 0.0,
    open var deliveryHours: @WriteWith<WorkingHoursRealmListParceler> RealmList<WorkingHours> = RealmList(),
    open var imageGallery: @WriteWith<ImageRealmListParceler> RealmList<Image> = RealmList(),
    @SerializedName("actions")
    open var actionsBasic: @WriteWith<ActionBasicRealmListParceler> RealmList<ActionBasic> = RealmList(),
    open var releaseDate: String? = "",
    open var slug: String? = "",
    open var link: String? = "",
) : RealmObject(), Serializable, Parcelable

fun Model.price(): String {
    return priceDouble?.toInt().toString().replace(",", "").replace(".", "")
}

@Parcelize
open class ModelStock(
    @PrimaryKey open var id: String = "",
    open var image: Image? = null,
    open var translations: Translation? = null,
    open var isEnabled: Boolean? = true,
    open var type: Type? = null
) : RealmObject(), Serializable, Parcelable


@Parcelize
open class ModelStockBasic(
    @PrimaryKey open var id: String = "",
    open var ModelTypeId:Int? = null
) : RealmObject(), Serializable, Parcelable

@Parcelize
open class ModelBasic(
    @PrimaryKey open var id: String = ""
) : RealmObject(), Serializable, Parcelable


@Parcelize
open class Type(
    @PrimaryKey open var id: String = "",
    open var icon: Image? = null,
    open var translations: Translation? = null,
    open var isEnabled: Boolean? = true,
    open var categoryId: Int? = -1,
    open var position: Int = -1,
    open var type: Int? = -1


) : RealmObject(), Serializable, Parcelable

fun Type.catgeory(): Category? {
    val local = LocalRepo()
    return this.categoryId?.let { local.getOne(it.toString()) }
}

@Parcelize
open class Action(
    @PrimaryKey open var id: String = "",
    open var icon: Image? = null,
    open var translations: Translation? = null,
    open var isEnabled: Boolean? = true,
    open var identifier: String = "",
    open var position: Int = -1
) : RealmObject(), Serializable, Parcelable

@Parcelize
open class ActionBasic(
    @PrimaryKey open var id: String = "",
    open var position: Int = -1,
    open var isEnabled: Boolean? = true
) : RealmObject(), Serializable, Parcelable


@Parcelize
open class ModelGallery(
    @PrimaryKey open var id: String = "",
    @SerializedName("imageGallery")
    open var images: @RawValue RealmList<Image> = RealmList<Image>(),
    open var isEnabled: Boolean? = true
) : RealmObject(), Serializable, Parcelable

@Parcelize
open class VideoGallery(
    @PrimaryKey open var id: String = "",
    @SerializedName("videoGallery")
    open var images: @RawValue RealmList<Image> = RealmList<Image>(),
    open var isEnabled: Boolean? = true
) : RealmObject(), Serializable, Parcelable

@Parcelize
class IncrementNViewResponse(
    @SerializedName("data")
    var data: Int? = 0
) : Serializable, Parcelable

@Parcelize
class Data(
    var nbViews: Int = 0
) : Serializable, Parcelable

object WorkingHoursRealmListParceler : RealmListParceler<WorkingHours> {
    override val clazz: Class<WorkingHours>
        get() = WorkingHours::class.java
}

object ImageRealmListParceler : RealmListParceler<Image> {
    override val clazz: Class<Image>
        get() = Image::class.java
}

object ActionRealmListParceler : RealmListParceler<Action> {
    override val clazz: Class<Action>
        get() = Action::class.java
}

object ActionBasicRealmListParceler : RealmListParceler<ActionBasic> {
    override val clazz: Class<ActionBasic>
        get() = ActionBasic::class.java
}





