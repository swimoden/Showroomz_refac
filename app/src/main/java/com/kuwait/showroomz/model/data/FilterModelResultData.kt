package com.kuwait.showroomz.model.data

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class FilterModelResultData(
    var brandName: String = "Result",
    var models: List<Model>
) : Serializable
@Parcelize
data class FilterAttributes(
    var budget: String,
    var ids: List<Brand>?,
    var types:List<Type>?,
    var category: Category?

): Serializable,Parcelable{
    override fun toString(): String {
        return "budget :$budget \n ids :${ids?.size} \n types : ${types?.size}"
    }
}