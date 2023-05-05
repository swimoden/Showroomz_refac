package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.model.data.Action
import com.kuwait.showroomz.model.data.Lang



class ActionSimplifier(private val action: Action) {
    var id =action.id
    var isEnglish = Shared.prefs.getString(MyApplication.LANG, "EN") == "EN"
    private var lang:Lang?=action.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
  var name =lang?.name
    var icon = action.let { action ->
        action.let {
             it.icon?.file
        }
    }

}