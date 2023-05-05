package com.kuwait.showroomz.viewModel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.Shared.Companion.modelsSimplifierList
import com.kuwait.showroomz.extras.toMSimplifier
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.realm.RealmObject

class SearchModelToCompareVM : ViewModel() {
    private val local = LocalRepo()

    var category: Category? = null
    val list = MutableLiveData<ArrayList<Model>>()
    var models: List<ModelSimplifier> = listOf()
    var aux = arrayListOf<Model>()
    val empty = MutableLiveData<Boolean>(false)
    init {
        //models= local.getAll<Model>()?.filter { m -> m.isEnabled == true } ?: arrayListOf()

    }

    fun filterInit() {
       /* val array = arrayListOf<Model>()
        models.forEach {
            if (ModelSimplifier(it).dealer?.category?.id == category?.id){
                array.add(it)
            }
        }
        models = array*/
        models = modelsSimplifierList.filter { s -> s.category?.id == category?.id && s.isEnabled }


       /* models = models.let {
            it.filter {
                ModelSimplifier(it).dealer?.category?.id == category?.id
            }
        }*/
    }
        fun searchByName(string: String?) {
            if (models.isEmpty()){
                modelsSimplifierList = (local.getAll<Model>() as ArrayList<Model> ).toMSimplifier()
                models = modelsSimplifierList.filter { s -> s.category?.id == category?.id && s.isEnabled }
            }
            aux.clear()
            if (string != "" && string != " ") {
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