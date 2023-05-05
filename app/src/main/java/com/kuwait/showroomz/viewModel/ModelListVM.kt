package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseListResponse
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.TrimResponse
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ModelListVM :ViewModel() {

    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val models = MutableLiveData<List<Model>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    private var auxList: List<Model> = arrayListOf()
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConectionError = MutableLiveData<Boolean>( false)
    val executor: Executor = Executors.newSingleThreadExecutor()
    fun getModels(usedFor: String, id: String) {
        loading.value = true
        empty.value=false
        val list = local.getAllByString<Model>("dealerData.id", id)
        if (list != null && list.size > 0) {
            loading.value = false
            error.value = ""
            models.value = list.filter { it.isEnabled==true }.sortedByDescending { it.position }
            auxList = list.filter { it.isEnabled==true }.sortedByDescending { it.position }

            list.forEach{
                getTrims(it.id)
            }
        }else{
            if (!NetworkUtils.instance.connected) {
                loading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }
        }

        val url = "model-datas-light-get"
        val json = JsonObject()
        json.addProperty("_page", 0)
        json.addProperty("itemsPerPage", 200)
        if (id.toInt() > 0){
            val array = JsonArray()
            array.add(id.toInt())
            json.add("brands", array)
        }

        disposable.add(
            service.getAllBrandModels(url, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Model>>>() {
                    override fun onComplete() {}

                    override fun onNext(obj: BaseListResponse<List<Model>>) {
                        obj.Result?.let { models ->
                            if (models.isNotEmpty())
                                setTranslationPrimaryKey(models, url, id, usedFor)
                            else if (list!!.isEmpty()) empty.value=true

                            if (list == null || list.size == 0) {
                                models.forEach{
                                    getTrims(it.id)
                                }
                            }
                        }
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                        if (list!!.isEmpty()) empty.value=true
                    }
                })
        )

    }
    fun setTranslationPrimaryKey(list: List<Model>, url: String, id: String, usedFor: String) {
        local.save(list) {
            if (list.isNotEmpty() && it) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }

            val listFromLocal = local.getAllByString<Model>("dealerData.id", id)
            loading.value = false
            error.value = ""
            empty.value=listFromLocal?.isEmpty()

            listFromLocal?.let{ modelsList ->
                models.value =  modelsList.filter { it.isEnabled==true }.sortedByDescending { it.position }
                auxList = modelsList.filter { it.isEnabled==true }.sortedByDescending { it.position }
            }
        }
    }
    override  fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
    fun search(txt: String) {
        if (txt.isEmpty()) {
            models.value = auxList
        } else {
            var filtered = auxList
            filtered = filtered.let { brands ->
                brands.filter {
                    ModelSimplifier(it).name.toLowerCase().contains(txt.toLowerCase()) &&it.isEnabled==true
                }
            }
            models.value = filtered.sortedByDescending { it.position }
            empty.value = filtered.isEmpty()
        }
    }

    fun getTrims(modelId: String) {

        if (NetworkUtils.instance.connected) {
            val url = "model-data/$modelId/$TRIMS_OFFERS"
            disposable.add(
                service.getModelTrims(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                        override fun onSuccess(response: TrimResponse) {
                            executor.execute {
                                val local2 = LocalRepo()
                                local2.saveObject(response) {}
                            }

                        }

                        override fun onError(e: Throwable) {

                        }
                    }
                )
            )
        }
    }
}