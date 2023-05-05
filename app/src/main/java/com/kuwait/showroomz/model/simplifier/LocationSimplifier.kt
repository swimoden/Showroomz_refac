package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.extras.convertToLocal
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Location
import com.kuwait.showroomz.model.data.Types
import com.kuwait.showroomz.model.local.LocalRepo

import java.util.*
import kotlin.collections.ArrayList


class LocationSimplifier(private var location: Location) {

    var local = LocalRepo()
    var id = location.id
    private var locationAddress = location.address
    private var lang = location.translations?.let{trans->
       if (isEnglish) trans.en else trans.ar
    }
    var name = lang?.name
     fun brand(): BrandSimplifier? {

        return location.dealerData?.let { BrandSimplifier(it) }
    }
    var brandName = if (brand() != null) brand()?.name else ""

    var latitude = locationAddress?.latitude
    var longitude = locationAddress?.longitude

    var workingHours = location.workingHours

    var addressName = if (brandName != "") "$brandName: $name" else name
    //var isBookingAvailable = location.status == 1

    var type = Types.findByValue(location.type)
    var hide = type == Types.SHOWROOM

    fun formattedWorkingHours(day:Int ):String{
        var newDay = day
        if (day == -1){
            newDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        }
        var whFormatted =  context.resources.getString(R.string.closed)
        workingHours?.let {
            for (item in it){
                if (item.days.contains(newDay)) {
                    item.days.let {
                        val fromHour = item.fromHour?.substring(0, 5)
                        val toHour = item.toHour?.substring(0, 5)
                        if (whFormatted == context.resources.getString(R.string.closed) ){
                            whFormatted = String.format(
                                context.resources.getString(R.string.whhours),
                                fromHour,
                                toHour
                            )

                        }else{
                            whFormatted += " | "
                            whFormatted += String.format(
                                context.resources.getString(R.string.whhours),
                                fromHour,
                                toHour
                            )
                        }

                    }
                }
            }
        }
        if (newDay == 5) whFormatted = String.format(context.resources.getString(R.string.friday), whFormatted)
        return whFormatted
    }
    /*fun formattedWorkingHours():String{
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var whFormatted = context.resources.getString(R.string.closed)
        workingHours?.let {
            for (item in it){
                item.days.let {
                    val  fromHour = item.fromHour?.substring(0,5)
                    val  toHour = item.toHour?.substring(0,5)
                    whFormatted = String.format(context.resources.getString(R.string.whhours), fromHour,toHour) //"$fromHour to $toHour"
                }
            }
        }
        if (day == 6) whFormatted = String.format(context.resources.getString(R.string.friday), whFormatted) //"Friday:$whFormatted"
        return whFormatted
    }*/
    fun getWorkingHoursArray(day:Int): ArrayList<String>  {
        val array:ArrayList<String> = arrayListOf()
        /*if (day==6){
            return array
        }*/
        val value = formattedWorkingHours(day).replace(context.resources.getString(R.string.to), ":")

        if (!value.contains(":") ) {
            return array
        }
        val parentContainer = value.split(" | ")
        for (item in parentContainer){
            val container = item.split(":")
            val firstPart = container[0].split( ":")
            val secondPart =  container[1].split( ":")
            if (container.size > 2) {
                val thirdPart = container[2].split(":")
                //val fourthPart = container[3].split(":")
                var x1 = firstPart[0].removeSuffix(" ").toInt()
                val x2 = secondPart[0].removeSuffix(" ")
                val third = thirdPart[0].removeSuffix(" ")
                val x3 = third.removePrefix(" ").toInt()
                //val x4 = firstPart[0].removeSuffix(" ").toInt()

                while (x1 < x3) {
                    if (x1 > 12) {
                        val c = x1 - 12
                        array.add(
                            String.format(
                                context.resources.getString(R.string.pm),
                                "$c".convertToLocal(),
                                x2.convertToLocal()
                            )
                        )
                    } else {
                        array.add(
                            String.format(
                                context.resources.getString(R.string.am),
                                "$x1".convertToLocal(),
                                x2.convertToLocal()
                            )
                        )
                    }
                    x1 += 1
                }
            }
        }

        return array
    }


}