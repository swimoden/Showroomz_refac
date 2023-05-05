package com.kuwait.showroomz.viewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kuwait.showroomz.R
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ModelVM : ViewModel() {
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
    var brand = MutableLiveData<Brand>()
    var pageC = 0
    val executor: Executor = Executors.newSingleThreadExecutor()
    private fun reloadLocal(page:Int, id: String){
        getModels(id)
       /* val list = local.getAllByString<Model>("dealerData.id", id)
        if (list != null && list.size > 0) {
            loading.value = false
            error.value = ""
            models.value = list.filter { it.isEnabled==true }.sortedByDescending { it.position }
            auxList = list.filter { it.isEnabled==true }.sortedByDescending { it.position }
            /*list.forEach{
                getTrims(it.id)
            }*/
        }else{
            if (!NetworkUtils.instance.connected) {
                loading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }
            if (page < 3){
                Handler(Looper.getMainLooper()).postDelayed({
                    pageC += 1
                    reloadLocal(pageC, id)
                },1000)

            }else{

                val prefix =
                    "dealerData.id="
                val url = "model-datas-light-get?_page=0&itemsPerPage=200&$prefix$id"
                Log.e("MODEL_URL",url)
                disposable.add(
                    service.getModelsByBrand(url)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Model>>() {
                            override fun onSuccess(models: List<Model>) {
                                if (models.isNotEmpty())
                                    setTranslationPrimaryKey(models, url, id)
                                else if (list!!.isEmpty()) empty.value=true
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
                //loading.value = false
               // empty.value=true
            }

        }*/
    }
    fun getModels( id: String) {

        empty.value=false
        val list = local.getAllByString<Model>("dealerData.id", id)
        if (list != null && list.size > 0) {
            loading.value = false
            error.value = ""
            models.value = list.filter { it.isEnabled==true }.sortedByDescending { it.position }
            auxList = list.filter { it.isEnabled==true }.sortedByDescending { it.position }
           // executor.execute {
               /* list.forEach{
                    getTrims(it.id)
                }*/
           // }

        }else{
            loading.value = true
            if (!NetworkUtils.instance.connected) {
                loading.value = false
                empty.value = true
                noConectionError.value = true
                return
            }

                //reloadLocal(pageC, id)


        }

        val array = JsonArray()
        array.add(id)
        val url = "model-datas-light-get"//String().format(AllModelsApiLight,page)
        val json = JsonObject()
        json.addProperty("_page", 0)
        json.addProperty("itemsPerPage", 200)
        json.add("brands", array)

        disposable.add(
            service.getAllBrandModels(url, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Model>>>() {
                    override fun onComplete() {}
                    override fun onNext(obj: BaseListResponse<List<Model>>) {
                        loading.value = false
                        obj.Result?.let { models ->
                            if (list?.isEmpty() == true){
                                if (models.isNotEmpty())
                                    setTranslationPrimaryKey(models, url, id)
                                else  empty.value = true
                            }else{

                                local.save(models){
                                   /* models.forEach{
                                        getTrims(it.id)
                                    }*/
                                }
                            }

                        }
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

    fun setTranslationPrimaryKey(list: List<Model>, url: String, id: String) {
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
                    ModelSimplifier(it).name.toLowerCase().contains(txt.toLowerCase())
                }
            }
            models.value = filtered
            empty.value = filtered.isEmpty()
        }
    }

    fun getTrims( modelId: String) {
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
                    })
            )
        }
    }

    fun getBrandBySlug(id: String,  action: (brand: Brand) -> (Unit)){
        loading.value = true
        val url = "$BRAND_DATAS_WITH_SLUG$id"
        disposable.add(
            service.getBrandBySlug(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Brand>>() {
                    override fun onSuccess(list: List<Brand>) {
                        loading.value = false
                        if (list.isNotEmpty()){
                            action.invoke(list[0])
                            local.saveObject(list[0]){ }
                        }else{
                            error.value = MyApplication.context.getString(R.string.error_occurred)
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("ModelDetailVM", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                    }

                })
        )
    }

}