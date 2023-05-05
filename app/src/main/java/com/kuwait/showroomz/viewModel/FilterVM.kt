package com.kuwait.showroomz.viewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.Shared.Companion.filtredList
import com.kuwait.showroomz.extras.Shared.Companion.modelsList
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class FilterVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val types = MutableLiveData<List<Type>>()
    val models = MutableLiveData<List<Model>>()
    val brands = MutableLiveData<List<Brand>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    var selectedBrands = ArrayList<Brand>()
    var selectedType = ArrayList<Type>()
    //var localList = ArrayList<Model>()
    lateinit var auxList: List<Brand>
    val empty = MutableLiveData<Boolean>()
    val noConnectionError = MutableLiveData<Boolean>(false)

    var budgetModel = String()
    private val local = LocalRepo()
    private val prefs = Shared()
    var list:ArrayList<Model>? = null

    fun refreshCate(category: Category, brandId:String) {
                filtredList.clear()
                 if (!prefs.existKey(category.id) && category.usedFor != "Selling") {

                     list = if (modelsList.isEmpty()){
                         local.getAll<Model>()?.filter {
                             val simp = ModelSimplifier(it)
                             simp.category?.id == category.id && simp.isEnabled
                         } as ArrayList<Model>
                     }else{
                         modelsList
                     }


                     if (!list.isNullOrEmpty()) {
                             prefs.setBool(category.id, true)
                            getTrimsWithIndex(0)
                         }
                 }
       // Handler(Looper.getMainLooper()).postDelayed({
            getBrands(brandId)
      //  }, 125)


    }
    fun getTrimsWithIndex(index:Int){
        list?.let {
            if (index < it.size)
                list?.get(index)?.id?.let {
                    getTrims(it, index)
                }
        }

    }
    fun getModelTypeByCategory(type: Int) {
        loading.value = true
        val list = local.getAll<Type>()?.filter {
             it.type == type && it.isEnabled == true
        }

        if (list != null && list.isNotEmpty()) {
            loading.value = false
            error.value = ""
            types.value = list.sortedBy { it.position }.filter { s-> s.isEnabled == true }
        }else{
            if (!NetworkUtils.instance.connected) {
               // noConnectionError.value = true
                loading.value = false
               // empty.value = true
                return
            }
        }
       /* val url = "model-types?type[]=${type}"
        Log.e("Filter", url)
        Log.e("types", "" + types.value?.size)
        disposable.add(
            service.getModelTypeByCategory(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Type>>() {
                    override fun onSuccess(list: List<Type>) {
                        Log.e("FilterSIze", "" + list.size)
                        if (list.isNotEmpty())
                            types.value = list.sortedBy { it.position }.filter { s-> s.isEnabled == true }
                            setTranslationPrimaryKey(list, url, type)
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("Filter", it) }
                        error.value = e.localizedMessage
                        loading.value = false
                    }
                })
        )*/
    }

    fun setTranslationPrimaryKey(list: List<Type>, url: String, type: Int) {
        local.save(list) {
            if (it) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            //val listFromLocal = local.getAll<Type>()?.filter { it.category?.type==type }

            val listFromLocal = local.getAll<Type>()?.filter {
                it.catgeory()?.id?.let { it1 ->
                    local.getOne<Category>(it1) }?.type == type && it.isEnabled == true}
            loading.value = false
            error.value = ""
            listFromLocal?.let { modelsList ->
                types.value = modelsList.sortedBy { it.position }
            }
        }
    }

    fun getBrands( id: String) {
        val list = local.getAllByString<Brand>("category", id)
        if (list != null && list.size > 0) {
            loading.value = false
            error.value = ""
            brands.value = list.filter { it.isEnabled==true }.sortedBy { BrandSimplifier(it).name }
            auxList = list.filter { it.isEnabled==true }.sortedBy {  BrandSimplifier(it).name  }
        }else{
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                return
            }
        }
        /*val url = "dealer-datas-light-get?_page=0&itemsPerPage=500&category.id=$id"
        disposable.add(
            service.getBrands(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Brand>>() {
                    override fun onSuccess(list: List<Brand>) {
                        if (list.isNotEmpty())
                            setBrandsTranslationPrimaryKey(list, url, id)
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                    }
                })
        )*/

    }

    private fun setBrandsTranslationPrimaryKey(list: List<Brand>, url: String, id: String) {

        local.save(list) {
            if (it) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            val brandList = local.getAllByString<Brand>("category.id", id)
            brandList?.let {
                brands.value = it.sortedByDescending { it.position }

            }
            loading.value = false
            error.value = ""

        }
    }
    fun search(txt: String) {
        if (txt.isEmpty()) {
            brands.value = auxList
        } else {
            var filtered = auxList
            filtered = filtered.let { brands ->
                brands.filter {
                    BrandSimplifier(it).name?.toLowerCase()?.contains(txt.toLowerCase())!!&&it.isEnabled==true
                }
            }
            brands.value = filtered.sortedBy {  BrandSimplifier(it).name  }
            empty.value = filtered.isEmpty()
        }
    }
    fun getTrims(modelId: String, index:Int) {
        if (NetworkUtils.instance.connected) {
            val url = "model-data/$modelId/$TRIMS_OFFERS"
            disposable.add(
                service.getModelTrims(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                        override fun onSuccess(response: TrimResponse) {
                            local.saveObject(response) {}
                            getTrimsWithIndex(index + 1)
                        }

                        override fun onError(e: Throwable) {
                            getTrimsWithIndex(index + 1)
                            print(e.localizedMessage)
                        }
                    }
                    )
            )
        }
    }
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}