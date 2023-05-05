package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Addon
import com.kuwait.showroomz.model.data.CarOption
import java.io.Serializable

class OptionSimplifier(private val  option: CarOption) {
    private var lang = option.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name = lang?.name

}
