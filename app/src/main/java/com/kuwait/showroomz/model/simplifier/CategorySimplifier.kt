package com.kuwait.showroomz.model.simplifier

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import com.kuwait.showroomz.extras.isEnglish
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.data.parentObj
import com.kuwait.showroomz.model.local.LocalRepo

class CategorySimplifier(private var category: Category) {

    private val local = LocalRepo()
    /*constructor( categoryBasic: CategoryBasic) : this(Category()) {
        category = local.getOne(categoryBasic.id)!!
    }*/

    var isCivilIdMandatory = category.isCivilIdMandatory
    var isKFh = category.isKFH
    var showExclusiveOnTop = category.showExclusiveOnTop
    var showAdsOnTop = category.showAdsOnTop
    var id = category.id
    private var nameEn: String? = category.translations?.let {
        it.en.let { name ->
            name?.name
        }
    }

    private var nameAr: String? = category.translations?.let {
        it.ar.let { name ->
            name?.name
        }
    }

    private var headerNameEn: String? = category.translations?.let {
        it.en.let { name ->
            name?.headerName
        }
    }

    private var headerNameAr: String? = category.translations?.let {
        it.ar.let { name ->
            name?.headerName
        }
    }
    var headerName = if (isEnglish) headerNameEn else headerNameAr
    var isVertical = category.showVerticalGallery?.let {
        it
    } ?: run {
        false
    }
    var colorString = category.bgColor ?: "#C7112A"
    var parentColorString = category.parentObj()?.bgColor ?: "#C7112A"

    @ColorInt
    fun setBgColor(): Int {

        return Color.parseColor(colorString)
    }

    @ColorInt
    fun setBgParentColor(): Int {

        return Color.parseColor(parentColorString)
    }

    var name = if (isEnglish) nameEn else nameAr
    var imgUrl = category.icon?.let {
        /*BASE_URL +*/ it.file?.replace("\\", "/")
    }

    fun bg(res: Drawable): GradientDrawable {
        val drawable = res as GradientDrawable
        drawable.setColor(setBgColor())
        return drawable
    }

    fun childes(): List<Category> {
        var childes: ArrayList<Category> = arrayListOf()

        local.getAll<Category>()?.let { childrens ->
            for (item in childrens) {
                if (item.parent == category.id)
                    childes.add(item)

            }
        }
        val x = childes.filter { category -> category.isEnabled == true }.sortedBy { it.position }
        return x
    }

    var selected = category.selected
    var hasModels = category.hasModels
    var usedFor = category.usedFor
    var color = setBgColor()

    var parent = category.parentObj()
    var showAdsOnScroll = category.showAdsOnScroll

}