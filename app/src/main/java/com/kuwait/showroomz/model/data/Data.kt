package com.kuwait.showroomz.model.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.delimiter
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.view.listener.RealmListParceler
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable
@Parcelize
open class Translation(
    @PrimaryKey open var custom_id: String = "",
    open var isEnabled:Boolean?=true,
    open var en: Lang? = null,
    open var ar: Lang? = null
) : RealmObject(),Serializable,Parcelable
@Parcelize
open class Lang(
    @PrimaryKey open var id: String = "",
    open var name: String? = "",
    open var headline: String? = "",
    open var description: String? = "",
    open var address: String? = "",
    open var country: String? = "",
    open var information: String? = "",
    open var headerName: String? = "",
    open var label: String? = "",
    open var value: String? = "",
    open var isEnabled:Boolean?=true,
    @SerializedName("Contents") open var content: @RawValue RealmList<String> = RealmList<String>()

) : RealmObject(), Parcelable



@Parcelize
@RealmClass
open class Image(
    @PrimaryKey open var id: String = "",
    open var isEnabled:Boolean?=true,
    open var file: String? = "",
    open var fileName: String? = "",
    open var name: String? = "",
    open var duration: String? = "",
    open var size: String? = "",
    open var createdAt: String? = ""

) : RealmObject(), Serializable, Parcelable,DataObject
@Parcelize
open class Address(
    @PrimaryKey open var id:String = "",   open var isEnabled:Boolean?=true,
    open var translations:Translation? = null,
    open var latitude:Double = 0.0,
    open var longitude:Double = 0.0
): RealmObject(), Serializable, Parcelable


@Parcelize
@RealmClass
open class WorkingHours(
    @PrimaryKey open var id:String = "",
    @SerializedName("weekdayIds") open var days: @RawValue RealmList<Int> = RealmList(), open var isEnabled:Boolean?=true,
    open var fromHour:String? = "",
    open var toHour:String? = ""

): RealmObject(), Serializable, Parcelable


data class DashBoardItem(
    val title:String,
    val list:List<Brand>?,
    val ads:List<Advertisement>?
)
data class ModelDashBoardItem(
    val title:String,
    val list:List<Model>?,
    val ads:List<Advertisement>?
)

data class UserCarDetails(
    val key:String,
    val value:String?
)
enum class Types(val value: Int) {
    SHOWROOM(10),
    QUICKSERVICE(30),
    WORKSHOP(20);

    companion object {
        private val types = values().associateBy { it.value }

        fun findByValue(value: Int) = types[value]
    }
}
enum class ClickType {
    ITEM,
    CALLBACK
}

data class OfferDetail(
    val txt:String,
    val amount:String,
    val type:Int){
    fun amountFormatted():String{
        if (amount.delimiter().isEmpty()){
            return  ""
        }
        return  amount.delimiter() + " " + if (isEnglish) "KWD" else "دينار"
    }
}
