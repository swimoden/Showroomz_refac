package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.android.parcel.WriteWith

@Parcelize
open class Advertisement(
    @PrimaryKey open var id: String? = null,
    open var imageGallery: @RawValue RealmList<Image> = RealmList<Image>(),
    open var video: String? = null,
    open var locationInApp: Int? = -1,
    open var categoryToDisplay: CategoryBasic? = null,
    open var categoryToGo: CategoryBasic? = null,
    open var mediaType: String? = null,
    open var mediaSize: Int? = -1,
    open var link: String? = null,
    open var modelToGo: ModelBasic? = null,
    open var brandToGo: BrandBasic? = null,
    open var startAt: String? = null,
    open var endAt: String? = null,
    open var allowExceedTarget: Boolean? = false,
    open var forMen: Boolean? = false,
    open var forWomen: Boolean? = false,
    open var impressionsNumber: Int? = -1,
    open var duration: Int? = 0,
    open var nbViews: Int? = 0,
    open var position: Int? = -1,
    open var status: Int? = -1,
    open var isRandom: Boolean? = false,
    open var allowSkip: Boolean? = false,
    open var banks: @RawValue RealmList<Bank> = RealmList<Bank>(),
    open var isEnabled: Boolean? = false,
    open var translations: Translation? = null,
    @SerializedName("actions")
    open var actionsBasic: @WriteWith<ActionBasicRealmListParceler> RealmList<ActionBasic> = RealmList()
) : RealmObject(), Parcelable

@Parcelize
open class DisplayedAdvertisement(
    @PrimaryKey open var id: String? = null
) : RealmObject(),
    Parcelable