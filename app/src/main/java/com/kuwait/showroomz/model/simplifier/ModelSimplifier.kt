package com.kuwait.showroomz.model.simplifier

import android.graphics.Color
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import io.realm.RealmList
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext
import kotlin.text.isNullOrEmpty

class ModelSimplifier(val model: Model) : Serializable {
    val local = LocalRepo()
    var id = model.id
    var order = model.position
    var actions: List<Action> = mapActions() //model.actions.filter { action -> action.isEnabled == true }

    fun mapActions(): List<Action> {
        val list = arrayListOf<Action>()
        val local = LocalRepo()
        model.actionsBasic.forEach {
            if (it.isEnabled == true) {
                val item = local.getOne<Action>(it.id)
                item?.let { it1 -> list.add(it1) }
            }
        }
        return list
    }
    fun getTrimPrice(): Int {
        val trimResponse = local.getOneWithPredicate<TrimResponse>(id, "id")
        val trims = trimResponse?.trims
        trims?.let{
            if (it.size > 0) {
                it.first()?.let {
                    return if (it.programs.size > 0) {
                        val sorted = it.programs.sortedBy { s -> s.monthlyPayment.toInt() }
                        sorted.first().monthlyPayment.toInt()
                    } else {
                        0
                    }
                }
            }
        }

        return 0
    }

    fun getOffers(): List<Offer>? {
        val trimResponse = local.getOneWithPredicate<TrimResponse>(id, "id")
        return trimResponse?.offers?.filter { s-> s.isEnabled == true }
    }


    var testDrive = model.testDriveStatus
    var deliveryHours = model.deliveryHours
    var isEmptyPrice = model.price() == "0" || model.price().isNullOrEmpty()
    var price = if (model.price() == "0") {
        ""
    } else {
        model.price().delimiter() + if (isEnglish) " KWD " else " د.ك "
    }
    var priceFloat = model.priceFloat
    var hasAllOffer = model.hasAllOffer
    var hasGallery = model.hasGallery
    var hasVideo = model.hasVideo
    var hasExclusiveOffer = model.hasExclusiveOffer
    var isNew = model.isNew
    var isPriceOffer = model.isPriceOffer
    var years = model.years
    var showActions = model.showActions
    var isComingSoon = model.isComingSoon
    var slug = model.slug
    var isEnabled = model.isEnabled ?: false



val priceFilter = if (getTrimPrice() > 0) getTrimPrice() else priceFloat?.toInt()
    /*private fun fetchImage(): String? {
        if (model.customImage != null) {
            model.customImage?.let {
                return BASE_URL + it.file
            }
        } else {
            return imageStock
        }
        return null
    }*/
    var modelStock = fetchModelStock()
    private fun fetchModelStock(): ModelStock? {
        return model.model?.id?.let {
            local.getOne(it)
        }
    }
    var typeId =  modelStock?.type?.id
    var modeltypeId = model.model?.ModelTypeId
    var image = if (model.customImage != null) {
        model.customImage?.let {
             it.file
        }?: run {
            ""
        }
    } else {
        modelStock?.let {
             it.image?.file
        }?: run {
            ""
        }
    }

    var imageStock:String? = modelStock?.let {
         it.image?.file
    }
    private var lang2 = model.translations?.let { trans ->
       // it.translations?.let { trans ->
            if (!isEnglish) trans.en else trans.ar
        //}
    }
    private var lang = model.translations?.let { trans ->
       // it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        //}
    }
    private var langType = modelStock?.let {
        it.type?.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var type = langType?.name ?: ""
    var name = lang?.name ?: ""
    var name2 = lang2?.name
    var isEmptyBookingAmount = model.bookingAmount.isNullOrEmpty()
    var bookingAmount =
        if (model.bookingAmount.isNullOrEmpty()) "" else model.bookingAmount + if (isEnglish) " KWD " else " د.ك "

    var internalImage =  model.internalImageUpload.let { model ->
        model?.let {
            if(it.isEmpty())
                ""
            else
            "$PANORAMA_URL$it"
        }
    }
    var externalImage =  model.externalImageLink ?: model.externalImageUpload
    /*if (!model.isExternalImageUpload) {
        model.externalImageLink ?: model.externalImageUpload

    } else {
        model.externalImageUpload.let { model ->
            model?.let {
                it
            }
        }
    }*/
    var dealer = fetchDealer()
    fun fetchDealer(): Brand? {
        return model.dealerData?.let {
        val local2 = LocalRepo()
            local2.getOne<Brand>(it.id)
        }
    }

    //var mBrand =  model.dealerData
    var brand = dealer?.let { BrandSimplifier(it) }
    /*if (model.brandData != null) model.brandData?.let { BrandSimplifier(it) } else model.dealerData?.let {
        BrandSimplifier(it)
    }*/
    var categoryParent = brand?.parentCat?.let {
       // val x = it.translations?.en?.name
        CategorySimplifier(it)
    }
    var category = brand?.cat?.let { CategorySimplifier(it) }
    var isexclisive = model.isExclusive
    var isoffer = model.isOffer
    private fun getExtraTxt(): String {
        if (isComingSoon == true){
            return context.getString(R.string.coming_soon)
        }
        if (isoffer) {
            return if (isEnglish) "Offers" else "عروض"
        }
        if (isexclisive) {
            return if (isEnglish) "Exclusive" else "حصرية"
        }
        return ""

    }
    var color = brand?.category?.setBgColor() ?: Color.parseColor("#C7112A")
    var extra: String = getExtraTxt()
    var searchName:String = name + name2 //modelStock?.translations?.ar?.name + modelStock?.translations?.en?.name
    var searchPrice = model.priceFloat
    private fun formattedDeliveryHours(
        day: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    ): String {
        var whFormatted = "Closed"
        model.deliveryHours.let {
            for (item in it) {
                item.days.let {
                    val fromHour = item.fromHour?.substring(0, 5)
                    val toHour = item.toHour?.substring(0, 5)
                    whFormatted = "$fromHour to $toHour"
                }
            }
        }
       // if (day == 6) whFormatted = "Friday- $whFormatted"
        return whFormatted
    }
    var phoennbr = brand?.phoneNbr

    private fun formattedWorkingHours(
        day: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    ): String {
        var whFormatted = "Closed"
        brand?.locations?.first()?.workingHours?.let {
            for (item in it) {
                item.days.let {
                    val fromHour = item.fromHour?.substring(0, 5)
                    val toHour = item.toHour?.substring(0, 5)
                    whFormatted = "$fromHour to $toHour"
                }
            }
        }
       // if (day == 6) whFormatted = "Friday- $whFormatted"
        return whFormatted
    }

    fun getWorkingHoursArray(day: Int): ArrayList<String> {
        val array: ArrayList<String> = arrayListOf()

        val value = formattedWorkingHours(day).replace("to", ":")

        if (!value.contains(":")) {
            return array
        }
        val container = value.split(":")
        val firstPart = container[0].split(":")
        val secondPart = container[1].split(":")
        val thirdPart = container[2].split(":")
        val fourthPart = container[3].split(":")
        var x1 = firstPart[0].removeSuffix(" ").toInt()
        val x2 = secondPart[0].removeSuffix(" ")
        var third = thirdPart[0].removeSuffix(" ")
        val x3 = third.removePrefix(" ").toInt()
        val x4 = firstPart[0].removeSuffix(" ").toInt()

        while (x1 < x3) {
            array.add("$x1:$x2")
            x1 += 1
        }

        return array
    }

    fun getDeliveryHoursArray(day: Int): ArrayList<String> {
        val array: ArrayList<String> = arrayListOf()

        val value = formattedDeliveryHours(day).replace("to", ":")

        if (!value.contains(":")) {
            return array
        }
        val container = value.split(":")
        val firstPart = container[0].split(":")
        val secondPart = container[1].split(":")
        val thirdPart = container[2].split(":")
        val fourthPart = container[3].split(":")
        var x1 = firstPart[0].removeSuffix(" ").toInt()
        val x2 = secondPart[0].removeSuffix(" ")
        val third = thirdPart[0].removeSuffix(" ")
        val x3 = third.removePrefix(" ").toInt()
        val x4 = firstPart[0].removeSuffix(" ").toInt()

        while (x1 < x3) {
            if (x1>12 ){
                val c = x1 - 12
                array.add(String.format(MyApplication.context.resources.getString(R.string.pm), "$c".convertToLocal(),x2.convertToLocal()))
            }else {
                array.add(String.format(MyApplication.context.resources.getString(R.string.am), "$x1".convertToLocal(),x2.convertToLocal()))
            }
            x1 += 1
        }

        return array
    }
    var x1 = dealer?.x1 ?: 0
    var x2 = dealer?.x2 ?: 0
    var link = model.link
}