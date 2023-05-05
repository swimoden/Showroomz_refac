package com.kuwait.showroomz.model.simplifier

import com.google.gson.annotations.SerializedName

data class UploadFileResponse (
    var status:String?="",
    var message:String?="",
    @SerializedName("Result") var data:Data?=Data()

)
data class Data(
    @SerializedName("fileName") var id:String?=""
)