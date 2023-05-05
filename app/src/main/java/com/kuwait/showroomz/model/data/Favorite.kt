package com.kuwait.showroomz.model.data

import android.os.Parcelable
import com.kuwait.showroomz.model.local.LocalRepo
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Favorite(
    @PrimaryKey var id: String? = "",
   // var client: User? = User(),
    var customerId:String = "",
    var modelData: ModelBasic? = null,
    open var isEnabled:Boolean?= true,
    var createdAt: String? = ""
) : RealmObject(), Parcelable

fun Favorite.client() : User?{
    val local = LocalRepo()
    return local.getOne(customerId)
}