package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.PreferredTimeTestDrive
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.local.LocalRepo
import java.util.*


class TestDriveSimplifier(var testDrive: TestDrive) {
    private val local= LocalRepo()
    var modelData = testDrive.modelData?.id?.let { local.getOne<Model>(it) }
    var model = modelData?.let { ModelSimplifier(it) }
    var preferredTime=Utils.instance.dateToString(testDrive.preferredTime?.let {
        Utils.instance.stringToDate(
            it,
            "yyyy-MM-dd'T'HH:mm:ss"
        )
    }!!, "dd/MM/yyyy hh:mm a")
    var creationDate=Utils.instance.dateToStringGMT(testDrive.createdAt?.let {
        Utils.instance.stringToDate(
            it,
            "yyyy-MM-dd'T'HH:mm:ss"
        )
    }!!, "dd/MM/yyyy hh:mm a")
    var status = testDrive.status
    var hasLocation= testDrive.location != null && testDrive.location?.address?.latitude != 0.0 && testDrive.location?.address?.longitude != 0.0
    var catId = model?.category?.id

    fun isExpired():Boolean {
        testDrive.preferredTime?.let {
            Utils.instance.stringToDate(
                it,
                "yyyy-MM-dd'T'HH:mm:ss"
            )

        }?.let { date ->
            return Date().after(date)
        }
        return false
    }
    var statusTitle = if (status == 10) {
        if (isExpired()) context.getString(R.string.expired) else context.getString(R.string.pending)
    } else {
        context.getString(R.string.successfully)
    }

}
class PreferredTimeSimplifier(var preferredTimeTestDrive: PreferredTimeTestDrive){
    var id=preferredTimeTestDrive.id
    var preferredTime=preferredTimeTestDrive.preferredTime
    private var lTime =preferredTimeTestDrive.preferredTime?.let {time->
        Utils.instance.stringToDate(time,"yyyy-MM-dd'T'HH:mm:ss")
            ?.let { Utils.instance.dateToString(it,"HH:mm") }}
    var time= if (lTime?.get(0)=='0'){
        lTime!!.removeRange(0,1)
    } else{
        var x = lTime ?: ""
        if (x.contains(":")) {
            val container = x.split(":")
            val x1 = container[0].toInt()
            val x2 = container[1]
            if (x1 > 12) {
                val f = x1 - 12
                x = "$f:$x2"
            }
        }
        x
    }

}