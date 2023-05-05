package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.data.Phone

class CallbackRequest(
    var client: String? = "", fullName: String, modelData: String? = null, phone: Phone

) : CallbackWithOutUserRequest(fullName, modelData, phone) {
    override fun toString(): String {
        return "client=$client,fullName=$fullName," +
                "modelData=$modelData," +
                "phone=${phone.code}+${phone.number}," + "civilId=$civilId" + "isKFH=$isKFH"
    }
}

open class CallbackWithOutUserRequest(

    var fullName: String,
    var modelData: String? = null,
    var phone: Phone,
    var civilId: String? = "",
    var email:String? = "",
    var isKFH:Boolean = false,
    var advertisement: String? = null

) {
    override fun toString(): String {
        return "fullName=$fullName," +
                "modelData=$modelData," +
                "phone=${phone.code}+${phone.number}," + "civilId=$civilId" + "isKFH=$isKFH"
    }
}

class LocationCallbackRequest(
    var client: String? = "", fullName: String,civilId: String?=null,location: String?=null, phone: Phone

) : LocationCallbackWithOutUserRequest(fullName, phone,location) {
    override fun toString(): String {
        return "client=$client,fullName=$fullName," +

                "phone=${phone.code}+${phone.number}"
    }
}

open class AdsCallbackRequestWithOutUserRequest(

    var fullName: String,
    var phone: Phone,
    var civilId: String? = "",
    var advertisement:String?=""

) {
    override fun toString(): String {
        return "fullName=$fullName," +
                "phone=${phone.code}+${phone.number}"
    }
}

class AdsCallbackRequest(
    var client: String? = "", fullName: String,civilId: String?=null,advertisement: String?=null, phone: Phone

) : AdsCallbackRequestWithOutUserRequest(fullName, phone,advertisement) {
    override fun toString(): String {
        return "client=$client,fullName=$fullName,advertisement=$advertisement" +
                "phone=${phone.code}+${phone.number}"
    }
}

open class LocationCallbackWithOutUserRequest(

    var fullName: String,
    var phone: Phone,
    var civilId: String? = "",
    var location:String?=""

) {
    override fun toString(): String {
        return "fullName=$fullName," +
                "phone=${phone.code}+${phone.number}"
    }
}

open class CallbackFinanceRequest(


    var civilId: String? = "",
    var fullName: String,
    var modelData: String? = "",
    var bank: String? = "",
    var totalCost: String? = "",
    var loanAmount: String? = "",
    var downpayment: String? = "",
    var installmentPeriod: String? = "",
    var installmentAmount: String? = "",
    var profit: String? = "",
    var phone: Phone,
    var isKFH:Boolean


)

class CallbackFinanceWithUserRequest(


    var client: String? = "",
    civilId: String? = "",
    fullName: String,
    modelData: String? = "",
    bank: String? = "",
    totalCost: String? = "",
    loanAmount: String? = "",
    downpayment: String? = "",
    installmentPeriod: String? = "",
    installmentAmount: String? = "",
    profit: String? = "",
    phone: Phone,
    isKFH: Boolean = false


) : CallbackFinanceRequest(
    civilId,
    fullName,
    modelData,
    bank,
    totalCost,
    loanAmount,
    downpayment,
    installmentPeriod,
    installmentAmount,
    profit,
    phone,
    isKFH
)

data class CallbackResponse(
    var status: String? = "",
    var message: String? = "",
    var data: CallbackData? = null
)


class CallbackData {
    var callback: Callback? = null
    var token: String? = null
}

