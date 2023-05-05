package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.delimiter
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo

class BookingSimplifier(var booking:Booking) {
    var local = LocalRepo()
    var program = booking.program()
    var model = booking.modelData()?.let {
        local.getOne<Model>(it.id)?.let{ model ->
            ModelSimplifier(model)
        }

    }
    var user = booking.client()?.let { UserSimplifier(it) }
    var date = Utils.instance.dateToString(booking.createdAt?.let {
        Utils.instance.stringToDate(
            it,
            "yyyy-MM-dd'T'HH:mm:ss"
        )
    }!!, "dd/MM/yyyy hh:mm a")
    var status = booking.status
    var amountPaid = booking.amountPaid?.let { priceFormatter(it) }
    var price = booking.price?.let { priceFormatter(it) }
    var civilID =booking.civilIdFile
    var driversLicense =booking.driversLicense
    var paymentMethod =booking.paymentMethod
    private fun priceFormatter(pPrice: String): String {
       /* var tPrice = pPrice
        if (pPrice.length > 3) {
            tPrice = pPrice.substring(0, pPrice.length - 3) + "," + pPrice.substring(
                pPrice.length - 3,
                pPrice.length
            )

        }*/
        return if (isEnglish) "${pPrice.delimiter()} KWD " else "${pPrice.delimiter()} د.ك "
    }
}