package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApplyForFinanceParams(

open var downpayment: String? = "",
open var loanAmount: String? = "",
open var installmentPeriod: String? = "",
open var totalCost: String? = "",
open var installmentAmount: String? = "",
open var profit: String? = "",
open var modelId: String? = "",
open var bankId: String? = ""
):Parcelable