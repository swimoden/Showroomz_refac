package com.kuwait.showroomz.viewModel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.Shared.Companion.brandsSimplifierList
import com.kuwait.showroomz.extras.Shared.Companion.modelsSimplifierList
import com.kuwait.showroomz.extras.toBSimplifier
import com.kuwait.showroomz.extras.toMSimplifier
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.realm.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchVM : ViewModel() {
    private val local = LocalRepo()
    private val prefs = Shared()
    var category: Category? = null
    val list = MutableLiveData<ArrayList<RealmObject>>()
    var brands : List<BrandSimplifier> = listOf()
    var models: List<ModelSimplifier> = listOf()
    var aux = arrayListOf<RealmObject>()
    val empty = MutableLiveData<Boolean>(false)

    init {
        filterInit()
       // models = local.getAll<Model>()?.filter { m -> m.isEnabled == true }
       // brands = local.getAll<Brand>()?.filter { b -> b.isEnabled == true }
    }

    fun filterInit(){
       // CoroutineScope(Dispatchers.IO).launch {
            models =
                modelsSimplifierList.filter { s -> s.category?.id == category?.id && s.isEnabled }
            brands =
                brandsSimplifierList.filter { b -> b.category?.id == category?.id && b.isEnabled == true }

       // }
    }

    fun searchByName(string: String?) {
        if (models.isEmpty()){
           // modelsSimplifierList = (local.getAll<Model>() as ArrayList<Model> ).toMSimplifier()
            models = modelsSimplifierList.filter { s -> s.category?.id == category?.id && s.isEnabled }
                // print(models.size)
        }

        if (brands.isEmpty()){
           // brandsSimplifierList = (local.getAll<Brand>() as ArrayList<Brand> ).toBSimplifier()
            brands = brandsSimplifierList.filter{ b -> b.category?.id == category?.id && b.isEnabled == true}
            //print(brands.size)
        }
        aux.clear()
        if (string == "" || string == " ") {
            list.value = aux
            empty.value = false
        } else {

            if (TextUtils.isDigitsOnly(string)) {
                var x = models.filter { modelS ->
                    (modelS.searchPrice!! <= string?.toDouble()!! && modelS.searchPrice!! > 0) || modelS.searchName.toUpperCase()
                        .contains(string.toUpperCase())

                }

                x.let {
                    it.forEach {
                        aux.add(it.model)
                    }
                }


            } else {
                val b = brands.filter {
                    it.searchName.toLowerCase()
                        .contains(string?.toLowerCase() ?: "")
                }
                b.let {
                    it.forEach {
                        aux.add(it.brand)
                    }

                }
                val x = models.filter {
                    it.searchName.toUpperCase().let{
                        it.contains(string!!.toUpperCase())
                    }

                }
                x.let {
                    it.forEach {
                        aux.add(it.model)
                    }

                }
            }
            list.value = aux
            empty.value = aux.isEmpty()
        }
    }
}