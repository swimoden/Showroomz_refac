package com.kuwait.showroomz.model.simplifier

import android.graphics.Color
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo

class BrandSimplifier( val brand:Brand) {
    private val local = LocalRepo()
    var id = brand.id
    var order = brand.position
    var stock = fetcStock() //if (brand.brand != null) brand.brand else brand.dealer


     private fun fetcStock():BrandStock? {
      return  brand.dealer?.let {
             local.getOne<BrandStock>(it.toString())
        }

    }

    var isClosed = brand.status == 30//has(brand.status , 8) == 8
    var iscomingSoon = brand.status == 20//has(brand.status , 4) == 4
    var logo = stock?.let {
        it.logo?.let { logo ->
            logo.file
        }
    }
    var image = if (brand.customImage != null){
         brand.customImage?.let {
              it.file
         }
    } else{
        stock?.let { stock ->
            stock.image?.let {
                it.file
            }
        }
    }
    private var lang = stock?.let{
        it.translations?.let { trans ->
             if (isEnglish) trans.en else trans.ar
        }
    }

    var name = lang?.name
    var desc = lang?.description

    var cat  = fetchCategory()//brand.category
    var parentCat = fetchCategory()?.parentObj()
    var parentSimpCategory = parentCat?.let { CategorySimplifier(it) }
     var category = fetchCategory()?.let { CategorySimplifier(it) }
     fun fetchCategory():Category?{
        return brand.category?.let {
            print(it)
            local.getOne(it)
        }
    }
    var catName = category?.name
    var showExclusiveOnTop = category?.showExclusiveOnTop
    var headerName = category?.headerName
    //var hasExclusiveOffer = order < 10

    var hasModels = category?.hasModels
    var isBrand = category?.usedFor == "Selling"
    //var locationUrl = if (isBrand) BRAND_LOCATION_API else DEALER_LOCATION_API
    //var hasOffers =  false
    var color = if (category != null) category?.colorString else "#C7112A"
    var selected = false
    var isexclisive = brand.isExclusive
    var isoffer = brand.isOffer

    var extra = when {
        isClosed -> context.getString(R.string.closed)
        iscomingSoon -> context.getString(
            R.string.coming_soon)
        isoffer -> context.getString(R.string.offers)
        else -> ""
    }
    var searchName=stock?.translations?.ar?.name+stock?.translations?.en?.name
    var locations=local.getAllByInt<Location>("brandId", brand.id.toInt())
    var phoneNbr = brand.phone?.code+" - "+ (brand.phone?.number ?: "")
    var showOffers = brand.showOffer
    var showAdsOnTop = category?.showAdsOnTop
    var showAdsOnScroll= category?.showAdsOnScroll
    var isEnabled =  brand.isEnabled

}