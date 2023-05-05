package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CompareResult(
    var title: String? = "",
    var value1: String? = "",
    var value2: String? = "",
    var isChoice:Boolean?=false,
    var isChoice2:Boolean?=false,
    var isChecked1:Boolean?=false,
    var isChecked2:Boolean?=false
) : Parcelable
