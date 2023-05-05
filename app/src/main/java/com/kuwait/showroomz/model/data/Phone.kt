package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmObject
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Phone (
   open var code:String?="",
   open var number:String?=""

): RealmObject(),Parcelable