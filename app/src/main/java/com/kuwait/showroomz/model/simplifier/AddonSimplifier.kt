package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Addon
import java.io.Serializable

class AddonSimplifier(val  addon: Addon) {
    private var lang = addon.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name = lang?.name
    var price = if (addon.cost == "0") {
        ""
    } else {
        addon.cost  + if (isEnglish) " KWD " else " د.ك "
    }
}
data class AddonsResult(
    var name: String?,var result:String?):Serializable