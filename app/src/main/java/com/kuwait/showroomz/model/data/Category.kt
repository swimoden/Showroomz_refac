package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.kuwait.showroomz.model.local.LocalRepo
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable


data class BaseResponse<T>(
    var StatusCode:String,
    var Message:String,
    var Result:T?,
)
/*data class BaseResponse< T : RealmModel>(
    var StatusCode:String,
    var Message:String,
    var Result:T?,
)*/

data class BaseListResponse< T : List<RealmModel>>(
    var StatusCode:String,
    var Message:String,
    var Result:T?,
)

@Parcelize
open class Category(
    // @SerializedName(value = "id")// to be removed when we have the same name tag in api response
    @PrimaryKey open var id: String = "",
    open var usedFor: String? = "",
    open var bgColor: String? = "",
    open var index: Int = -1,
    open var position: Int = -1,
    open var hasModels: Boolean? = false,
    open var parent: String? = "",
    open var icon: Image? = null,
    open var translations: Translation? = null,
    open var showVerticalGallery: Boolean ?= false,
    open var isCivilIdMandatory: Boolean? = false,
    open var showExclusiveOnTop: Boolean? = false,
    open var showOffer: Boolean = false,
    open var showAdsOnTop: Boolean = false,
    open var showAdsOnScroll: Boolean = false,
    open var isForAppraisal: Boolean = false,
    open var isKFH: Boolean = false,
    @Ignore var selected: Boolean = false,
    open var isEnabled:Boolean?=true,
    var type: Int? = 0,
    open var slug: String? = ""
) : RealmObject(), Parcelable

@Parcelize
open class CategoryBasic (
    @PrimaryKey open var id: String = ""
) : RealmObject(), Serializable,Parcelable

fun Category.parentObj(): Category?{
    val local = LocalRepo()

    if(parent == "0"){
        return null
    }
    this.parent?.let {
        val x = local.getParent<Category>(it)?.translations?.en?.name
        print(x)
    }

    return this.parent?.let {

        local.getParent(it)
    }
}