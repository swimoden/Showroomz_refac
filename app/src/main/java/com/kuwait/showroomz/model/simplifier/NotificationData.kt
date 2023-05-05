package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationData (
    var modelData:String?="",
    var locationInApp:String?= null,
    var schedule:String?="",
    var bank:String?="",
    var body:String?="",
    var from:String?="",
    var title:String?="",
    var dealerData:String?="",
    var section:Section?=null,
    var callback_id:String? = null,
    var bank_id:String? = null,
    var link:String? = null,
    var navigateToLink:String?="",var parent_id:String?="",
    var id:String?=""
):Parcelable{
    override fun toString(): String {
        return "NotificationData(modelData=$modelData, locationInApp=$locationInApp, schedule=$schedule, bank=$bank, body=$body," +
                " from=$from, title=$title, dealerData=$dealerData, section=$section)"
    }
}
@Parcelize
data class Section(
    var parent_id:String?="",
    var id:String?=""

):Parcelable