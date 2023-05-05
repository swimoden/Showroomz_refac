package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.model.data.Lang
import com.kuwait.showroomz.model.data.Type

class TypeSimplifier(private val type:Type) {
    var isEnglish = Shared.prefs.getString(MyApplication.LANG, "EN") == "EN"
    var id=type.id
    private var lang: Lang?=type.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name =lang?.name
    var icon = type.let { action ->
        action.let {
            it.icon?.file
        }
    }
}