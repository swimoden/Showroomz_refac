package com.kuwait.showroomz.model.data

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable

@Parcelize
open class Bank(
    @PrimaryKey open var id: String = "",
    open var logo: Image? = null,
    open var position: Int? = -1,
    open var downPayment: String = "0",
    open var ratio: @RawValue RealmList<Double> = RealmList(),
    open var translations: Translation? = null,
    open var isPartner: Boolean? = false,
    open var isEnabled: Boolean? = true,
    open var isClosed: Boolean? = false,
    open var allowSemiApproval: Boolean? = false,
    open var allowLoanToExpat: Boolean? = false,
    open var allowLoanToPrivateSector: Boolean? = false,
    open var loanMinimumSalary: Int = -1,
    open var isFromMasterData: Boolean? = false,
    open var industries: @RawValue RealmList<Industry> = RealmList()

) : Serializable, RealmObject(), Parcelable

@Parcelize
open class Industry(
    @PrimaryKey open var id: String = "",
    open var translations: Translation? = Translation()
) : Serializable, RealmObject(), Parcelable