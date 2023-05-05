package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.*
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class TrimSimplifier(var trim: Trim) : Serializable, Parcelable {
    var id = trim.id
    var colors = trim.colors
    fun getSpecification():List<Spec> {
        val array: ArrayList<Spec> = arrayListOf()
        for (item in trim.specs) {
            val x = SpecSimplifier(item)

            if (x.contents.isNotEmpty()){
                array.add(item)
            }
        }
        return array
    }
    var specs = getSpecification().sortedBy { it.position }
    private var lang = trim.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }

    var name = lang?.name
    var position = trim.position
    var services = trim.services
    var programs = trim.programs
    var price = priceFormatter("${trim.price}")

    private fun priceFormatter(pPrice: String): String {
        //var tPrice = pPrice.delimiter()
        /*if (pPrice.length > 3) {
            tPrice = pPrice.substring(0, pPrice.length - 3) + "," + pPrice.substring(
                pPrice.length - 3,
                pPrice.length
            )

        }*/
        return if (isEnglish) "${pPrice.delimiter()} KWD " else "${pPrice.delimiter()} د.ك "
    }
}

class SpecSimplifier(var spec: Spec) {
    var type = spec.type
    var id = spec.id
    var contents = spec.contents.filter { s ->
        val c = SpecContentSimplifier(s)
        (c.value != "-" && c.value != "" && c.value != " " && c.isMultiple != true) || (c.isMultiple == true && c.ischoice == true && c.ischecked == true && c.value != "-")
    }
    var position = spec.position
    private var lang = spec.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name = lang?.name
    var order =
        if (spec.translations?.en?.name?.toUpperCase() == "driveTrain".toUpperCase() || spec.translations?.en?.name?.toUpperCase() == "Drive train".toUpperCase()) 0
        else if (spec.translations?.en?.name?.toUpperCase() == "exterior".toUpperCase() || spec.translations?.en?.name?.toUpperCase() == "exterior features".toUpperCase()) 1
        else if (spec.translations?.en?.name?.toUpperCase() == "interior".toUpperCase() || spec.translations?.en?.name?.toUpperCase() == "interior features".toUpperCase()) 2
        else if (spec.translations?.en?.name?.toUpperCase() == "safety".toUpperCase()) 3
        else 1000
  //  var isArabicName= name?.let { Utils.instance.isProbablyArabic(it) }
}

class SpecContentSimplifier(var content: SpecContent) {
    var id = content.id
    var translations = if (isEnglish) content.translations?.en else content.translations?.ar
    var value = translations?.value
    var isMultiple = content.isMultiple
    var ischoice = content.isChoice
    var ischecked = content.isChecked
}

class TrimTranslationSimplifier(var translation: Translation) {
    private var lang = if (isEnglish) translation.en else translation.ar
    var label = lang?.name?.capitalizeFirstLetter()
        // lang?.label?.get(0)?.toUpperCase()?.plus((lang?.label?.substring(1) ?: (lang?.label ?: "")))
    var value = if (label == null){lang?.value?.replace("\r", "\n")/*?.replace("(?m)^[ \t]*\r?\n", "")*/ } else {lang?.value}
    var isArabicValue =  !isEnglish // Utils.instance.isProbablyArabic(value?:"")
    var id = lang?.id
}

class ColorSimplifier(color: Color) {
    var topColor = android.graphics.Color.parseColor(color.top)
    var bottomColor = android.graphics.Color.parseColor(color.bottom)
}
//36303934
class ServiceSimplifier(var service: Service) {
    var image = service.let { service ->
        service.icon?.let {
            it.file
        }
    }
    private var lang = service.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }

    var name = lang?.name
}

class TrimProgramSimplifier(var program: Program?) {
    var payment = program?.monthlyPayment?.toInt().toString() + if (isEnglish) {
        " KWD "
    } else {
        " د.ك "
    }
    var contractPeriod = program?.contractPeriod
    var mileageLimit = if (program?.mileageLimit == null || program?.mileageLimit == "-") {
        ""
    } else {
        program?.mileageLimit?.delimiter()
    }
    var id = program?.id

}

