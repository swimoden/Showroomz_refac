package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Lang
import com.kuwait.showroomz.model.data.Offer

class OfferSimplifier(offer: Offer) {
    var banks = offer.banks
    var discount =offer.discountValue + " "+ if (isEnglish) "KWD" else "د.ك"
    var intDiscount = offer.discountValue ?: "0"
    var isInCorporation = offer.isInCorporation
    var id = offer.id
    private var lang: Lang? = offer.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name = lang?.name
    var icon = offer.let { offer ->
        offer.let {
             it.icon?.file
        }
    }
    var contents = lang?.content
    var isExclusive = offer.type == 2
    var type = offer.type
    var discountValue = intDiscount.toDouble()
}