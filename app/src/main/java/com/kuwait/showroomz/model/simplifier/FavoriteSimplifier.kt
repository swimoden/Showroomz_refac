package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.model.data.Favorite
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.client
import com.kuwait.showroomz.model.local.LocalRepo

class FavoriteSimplifier(var  favorite: Favorite) {
    val local = LocalRepo()
    var model = getModel()?.let { ModelSimplifier(it) } ?: run{ ModelSimplifier(Model())}
    private fun getModel(): Model?{
        return favorite.modelData?.id?.let { local.getOne<Model>(it) }
    }
    var id =favorite.id
    var user= favorite.client()?.let { UserSimplifier(it) }
    var image = model.image
    var categoryParentName = model.categoryParent?.let { it.name }
    var name = model.name
    var price = model.price
}