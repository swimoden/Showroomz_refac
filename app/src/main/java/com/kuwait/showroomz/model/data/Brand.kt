package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
@Parcelize
open class Brand (
    @PrimaryKey open var id: String = "",
    open var position: Int = -1,
    open var status: Int = -1,
    open var x1: Int = 0,
    open var x2: Int = 0,
    open var customImage: Image? = null,
    open var category: String? = "",
    open var dealer: String? = "",
    open var isExclusive:Boolean = false,
    open var isEnabled:Boolean?=true,
    open var isOffer:Boolean = false,
    open var showOffer: Boolean = false,
    open var phone:Phone? = null,
    open var slug: String? = ""
) : RealmObject(), Serializable,Parcelable

@Parcelize
open class BrandStock (
    @PrimaryKey open var id: String = "",
    open var image: Image? = null,
    open var logo: Image? = null,
    open var translations: Translation? = null,   open var isEnabled:Boolean?=true,
    open var position: Int = -1
) : RealmObject(), Serializable,Parcelable


@Parcelize
open class BrandStockBasic (
    @PrimaryKey open var id: String = ""
) : RealmObject(), Serializable,Parcelable



@Parcelize
open class BrandBasic (
    @PrimaryKey open var id: String = ""
) : RealmObject(), Serializable,Parcelable

