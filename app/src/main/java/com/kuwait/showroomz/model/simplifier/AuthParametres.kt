package com.kuwait.showroomz.model.simplifier

import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.data.Data
import com.kuwait.showroomz.model.data.Phone
import com.kuwait.showroomz.model.data.User

data class LoginRequest (
    var email: String,
    var password: String//,
    //var serialNumber: String
)

data class LoginResponse (
    var token:String,
    var user:User
)
data class ResetPasswordRequest (
    var email:String

)
data class ResetPasswordResponse (
    var status:String,
    var message:String,
    var Success:Boolean
)
data class ChangePasswordRequest (
    var email: String?,
    var password:String,
    var newPassword:String
)

data class ResetPasswordRequestWithOtp (
    var email: String?,
    var password:String,
    var otp:String
)
data class RegisterRequest(
    var email: String,
    var fullName: String,
    var password: String,
    var serialNumber: String,
    var phone: Phone


)
data class RegisterResponse(
    var status:String,
    var message:String,
    var  data: RegisterData

)

data class RegisterData (
    var user:User,
    var token:String

)
data class ErrorRegisterResponse(
    var status:String,
    var message:String,
    @SerializedName("data")
    var  data: ErrorRegisterData
)

data class ErrorRegisterData
   ( var  errors: ErrorResponse)

data class ErrorResponse (
    var email:String?="",
    var phone:String?="",
    var plainPassword:String?=""
)

