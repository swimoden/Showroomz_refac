package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.Shared.Companion.filtredList
import com.kuwait.showroomz.extras.Shared.Companion.modelsList
import com.kuwait.showroomz.extras.Shared.Companion.modelsSimplifierList
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class FilterResultViewModel : ViewModel() {
    val empty = MutableLiveData<Boolean>()
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    private var localList = ArrayList<Model>()

    val models = MutableLiveData<List<Model>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>(true)
    val noConnectionError = MutableLiveData<Boolean>(false)
    var category: Category? = null
    val shared = Shared()

    fun refreshCate(category: Category) {
        /*this.category = category
         run {
            val list = local.getAll<Model>() as ArrayList<Model>
            localList =
                list.filter { ModelSimplifier(it).category?.id == category.id } as ArrayList<Model>
        }*/
    }


    fun filterModels(attr: FilterAttributes) {
        // postSearchLog(attr)
        loading.value = true
        val listIds = ArrayList<String>()
        attr.ids?.let {
            for (item in it) {
                listIds.add(item.id)
            }
        }
        localList = local.getAll<Model>() as ArrayList<Model>

        var list = localList
        for (item in localList) {
            if (listIds.contains(item.dealerData?.id)) {
                list.add(item)
            }
        }

        attr.types?.let {
            if (it.isNotEmpty())
                list = filterModelsByType(list, it)
        }

        if (!attr.budget.isNullOrEmpty()) {
            if (!attr.budget.isNullOrEmpty())
                list = filterByBudget(list, attr.budget)
        }
        if (list.size == 0) {
            filterModelsFromApi(attr)
        } else {
            loading.value = false
            models.value = list
            if ((models.value as ArrayList<Model>).isEmpty()) {
                empty.value = true
            }
        }

    }

    private fun postSearchLog(attr: FilterAttributes) {
        if (!NetworkUtils.instance.connected) {
            // noConnectionError.value = true
            //loading.value = false
            return
        }
        val request = JsonObject()
        request.addProperty("serialNumber", DeviceManger.getDeviceId())
        if (attr.budget != "")
            request.addProperty("budget", attr.budget)
        val types = JsonArray()
        val dealers = JsonArray()
        if (attr.types?.isNotEmpty() == true) {
            attr.types?.forEach {
                types.add(it.id)
            }
            request.add("modelTypes", types)
        }
        if (attr.ids?.isNotEmpty() == true) {
            attr.ids?.forEach {
                dealers.add(it.id)
            }
            request.add("dealerDatas", dealers)
        }

        disposable.add(
            service.postSearchLog(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Log.e("postSearchLog", "onSuccess: ${t.string()}")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("postSearchLog", "onError:  ${e.localizedMessage}")
                    }


                })
        )
    }

    private fun filterByBudget(
        list: java.util.ArrayList<Model>,
        budget: String
    ): java.util.ArrayList<Model> {
        var model = ArrayList<Model>()
        list.forEach {
            if (it.price().replace(",", "")
                    .toDouble() <= budget.replace(",", "").toDouble()
            )
                model.add(it)
        }
        return model
    }

    fun filterModelsFromApi(attr: FilterAttributes) {

        loading.value = true
        val url = "model-datas-light-get"//String().format(AllModelsApiLight,page)
        val json = JsonObject()
        json.addProperty("_page", 0)
        json.addProperty("itemsPerPage", 200)
        attr.category?.id?.let {
            json.addProperty("dealerDataCategoryId", it)
        }

        attr.ids?.let {
            var  added = false
            val array = JsonArray()
            it.forEach {
               if (it.id.toInt() > 0){
                   array.add(it.id.toInt())
                   added = true
               }
            }
            if (added) {
                json.add("brands", array)
                added = false
            }
        }

        attr.types?.let {
            var  added = false
            val array = JsonArray()
            it.forEach {
                if (it.id.toInt() > 0){
                    array.add(it.type)
                    added = true
                }
            }
            if (added) {
                json.add("types", array)
                added = false
            }
        }
        if (!attr.budget.isNullOrEmpty()) {
            json.addProperty("budget", attr.budget)

        }



        disposable.add(
            service.getAllBrandModels(url, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Model>>>() {
                    override fun onComplete() {}

                    override fun onNext(obj: BaseListResponse<List<Model>>) {
                        loading.value = false
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                setTranslationModelsPrimaryKey(list, url)
                            }
                            val model = ArrayList<Model>()
                            if (!attr.budget.isNullOrEmpty()) {
                                list.forEach {
                                    if (it.price().replace(",", "")
                                            .toDouble() <= attr.budget.replace(",", "").toDouble()
                                    )
                                        model.add(it)
                                }
                            } else model.addAll(list)
                            models.value = model
                            filtredList = model
                            if ((models.value as ArrayList<Model>).isEmpty()) {
                                empty.value = true
                            }
                        }

                    }

                    override fun onError(e: Throwable) {
                        if (!NetworkUtils.instance.connected) {
                            noConnectionError.value = true
                            loading.value = false
                            empty.value = true
                            return
                        }
                        loading.value = false
                        error.value = e.localizedMessage
                        empty.value = true
                    }

                })
        )

    }

    fun filterFromLocal(attr: FilterAttributes) {
        if (filtredList.isEmpty()) {
            if (modelsList.isNotEmpty()) {
                val allModels = modelsSimplifierList.filter{ model ->
                    /*val x = model.dealer
                    val xx = model.brand?.category
                    val xxx = attr.category
                    val xxxx = attr.category?.id*/

                    model.brand?.category?.id == attr.category?.id && model.isEnabled
                }
                var filteredByType = arrayListOf<ModelSimplifier>()
                var filteredByBrand = arrayListOf<ModelSimplifier>()
                val filteredByPrice = arrayListOf<ModelSimplifier>()
                val result = arrayListOf<Model>()
//            loading.value=false
                if (attr.types?.isNotEmpty()!!) {
                    attr.types!!.forEach {
                        allModels.filter { model ->
                            model.modeltypeId?.let { t ->
                                "$t" == it.id
                            } == true

                        }.let { it1 ->
                            filteredByType.addAll(
                                it1
                            )
                        }
                    }
                } else filteredByType = allModels as ArrayList<ModelSimplifier>

                if (!attr.ids.isNullOrEmpty()) {
                    attr.ids!!.forEach {
                        filteredByBrand.addAll(filteredByType.filter { model -> model.brand?.id == it.id })
                    }
                } else filteredByBrand = filteredByType

                if (!attr.budget.isNullOrEmpty()) {
                    filteredByPrice.addAll(filteredByBrand.filter { model ->

                        model.priceFilter!! <= attr.budget.toDouble() && model.priceFilter > 0 //&& model.category?.id == attr.category?.id
                    })
                } else filteredByPrice.addAll(filteredByBrand/*.filter { model ->

                    model.dealer?.category?.id == attr.category?.id
                }*/)

                if (filteredByPrice.isNotEmpty()) {
                    loading.value = false
                    filteredByPrice.forEach{ simp ->
                        result.add(simp.model)
                    }
                    models.value = result
                    filtredList = result
                } else {
                   // if (category?.usedFor == "Selling") {
                        filterModelsFromApi(attr)
                   /* } else {
                        loading.value = false
                        empty.value = true
                    }*/
                    //
                }
            } else {
               // if (category?.usedFor == "Selling") {
                    loading.value = true
                    filterModelsFromApi(attr)
                /*} else {
                    loading.value = false
                    empty.value = true
                }*/
            }
        }else{
            loading.value = false
            models.value = filtredList
        }
    }

    private fun filterModelsByType(
        localList: ArrayList<Model>,
        types: List<Type>
    ): ArrayList<Model> {
        var list = ArrayList<Model>()
        localList.forEach {
            val models = ModelSimplifier(it)
            if (types.contains(models.modelStock?.type)) {
                list.add(it)
            }
        }
        return list
    }

    fun setTranslationModelsPrimaryKey(list: List<Model>, url: String) {
        local.save(list) {
            if (it && list.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
        }

    }



    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }


}