package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.IMAGES
import com.kuwait.showroomz.extras.MEDIA
import com.kuwait.showroomz.model.data.User

class UserSimplifier(var user:User) {
    var fullName=user.fullName
    var email=user.email

    var phone = (user.phone?.code ?: "")+" - "+ (user.phone?.number ?: "")
    var image =if (user.image!=null) BASE_URL+BuildConfig.API_VERSION+ MEDIA+"/"+user.image?.id else user.image?.id
    var civilId = user.civilID?:""
//    var phone =user.phone?.code+" - "+(user.phone?.number?.substring(0,4)+ " "+user.phone?.number?.substring(4, user.phone!!.number!!.length))

}