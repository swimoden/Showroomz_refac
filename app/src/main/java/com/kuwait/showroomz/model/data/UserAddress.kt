package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class UserAddress (
    @PrimaryKey var id:String?="",
    var name:String?="",
    var type:String?="",
    var address:String?="",
    var latitude:String?="",
    var longitude:String?="",
    var postalCode:String?="",   open var isEnabled:Boolean?=true,
    var client:User?=User(),
    var clientId:String?=""

):RealmObject(),Parcelable