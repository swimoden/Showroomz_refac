package com.kuwait.showroomz.model.simplifier

import androidx.core.text.isDigitsOnly
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo

class AdsSimplifier(val advertisement: Advertisement) {
    var local = LocalRepo()
    var actions: List<Action> = mapActions()

    private var lang = advertisement.let {
        it.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var id = advertisement.id
    var name = lang?.name
    var headline = lang?.headline
    fun mapActions(): List<Action> {
        val list = arrayListOf<Action>()
        val local = LocalRepo()
        advertisement.actionsBasic.forEach {
            if (it.isEnabled == true) {
                val item = local.getOne<Action>(it.id)
                item?.let { it1 -> list.add(it1) }
            }
        }
        return list
    }

    var link = advertisement.link

    var modelVm = fetchModel()?.let { ModelSimplifier(it) }
     fun fetchModel(): Model? {
        val x = advertisement.modelToGo?.id?.let { local.getOne<Model>(it) }
        return x
    }

    var categorVm = fetchCategory()?.let { CategorySimplifier(it) }
     fun fetchCategory(): Category? {
        val x = advertisement.categoryToGo?.id?.let { local.getOne<Category>(it) }

        return x
    }

    var categorVmdisplay = fetchCategoryDisplay()?.let { CategorySimplifier(it) }
    fun fetchCategoryDisplay(): Category? {
        val x = advertisement.categoryToDisplay?.id?.let { local.getOne<Category>(it) }
        return x
    }
    var brandVm = fetchBrand()?.let { BrandSimplifier(it) }
     fun fetchBrand(): Brand? {
         return advertisement.brandToGo?.id?.let { local.getOne<Brand>(it) }
    }
    fun navigateToLink():Boolean {
        link?.let {
            return it != "" && !it.isDigitsOnly()
        } ?: run{
            return false
        }
    }

}