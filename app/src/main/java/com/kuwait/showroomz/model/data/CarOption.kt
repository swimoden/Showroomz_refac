package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
open class CarOption(
    @PrimaryKey
    open var id: String = "",
    open var translations: Translation? = null,
    open var position: Int? = -1,
    open var isEnabled: Boolean?=true
) : RealmObject(), Parcelable