package com.kuwait.showroomz.model.simplifier

import com.google.gson.annotations.SerializedName
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.data.User

data class TestDriveParamsResponse(
    var status: String? = "",
    var message: String? = "",
    @SerializedName ("Result") var data: TestDriveData? = null

)

data class TestDriveData(
    var testDrive: TestDrive? = null,
    var token: String? = "",
    var user: User? = User()
)
