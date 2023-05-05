package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class TestDrive (
    @PrimaryKey  open var id:String?="",
    @SerializedName("type") open var discr:String?="",
    open var fullName:String?="",
    open var email:String?="",
    open var preferredTime:String?="",
    open var createdAt:String?="",
    open var status:Int?=0,
    @SerializedName("showroom") open var location:Location?= Location(),
    open var isEnabled:Boolean?=true,
    open var modelData:ModelBasic?=null,
    var client: User? = User()



):Parcelable,RealmObject()
@Parcelize
open class PreferredTimeTestDrive(
    @PrimaryKey  open var id:String?="",
    open var preferredTime:String?=""
):Parcelable,RealmObject()