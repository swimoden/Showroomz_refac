package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
open class Addon(
    @PrimaryKey
    open var id: String = "",
    open var cost: String = "",
    open var icon: Image? = null,
    open var translations: Translation? = null,
    open var modelDatas: @RawValue RealmList<WorkingHours>? = null,
    open var isCollection: Boolean? = false, open var isEnabled: Boolean? = true,
    open var position: Int? = -1
) : RealmObject(), Parcelable