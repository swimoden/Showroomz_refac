package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Industry

class BankSimplifier(private var bank: Bank?) {
    var id = bank?.id
    private var lang = bank?.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name = lang?.name
    var position = bank?.position
    var downPayment = bank?.downPayment
    var image = bank?.let { bank ->
        bank.logo?.let {
             it.file
        }
    }
    var ratio = bank?.ratio
    var isPartner = bank?.isPartner
    var isClosed = bank?.isClosed
    fun setBank(pBank: Bank?){
        if (pBank != null) {
            this.bank=pBank
        }
    }


}
class IndustrySimplifier(private var indusry: Industry?){
    val id=indusry?.id
    private var lang = indusry?.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
   var name = lang?.name
}