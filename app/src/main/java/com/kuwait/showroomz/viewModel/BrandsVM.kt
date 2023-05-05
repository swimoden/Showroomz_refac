package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Category
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.TrimResponse
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class BrandsVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val brands = MutableLiveData<List<Brand>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    var auxList: List<Brand> = emptyList()
    val empty = MutableLiveData<Boolean>()
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConectionError = MutableLiveData<Boolean>(false)
    fun isConnected() = prefs.existKey(USER_ID)
    fun refresh(usedFor: String, id: String = "") {
        fetchBrands(usedFor, id)
    }

    private fun fetchBrands(usedFor: String, id: String = "") {
        loading.value = true
        empty.value=false
        val idc = id
        val listFromLocal = local.getAllByString<Brand>("category", idc)
        if (listFromLocal != null && listFromLocal.size > 0) {
            loading.value = false
            error.value = ""
            brands.value = listFromLocal.filter { it.isEnabled==true }.sortedByDescending { it.position }
            auxList = listFromLocal.filter { it.isEnabled==true }.sortedByDescending { it.position }
        }else{

            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.value = false
                empty.value = true
                return
            }else{
                loading.value = false
                empty.value = true
                brands.value = emptyList()
            }
        }
        /*val url = "dealer-datas-light-get?_page=0&itemsPerPage=500&category.id=$id"
        disposable.add(
            service.getBrands(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Brand>>() {
                    override fun onSuccess(list: List<Brand>) {
                        loading.value = false
                        if (list.isNotEmpty())
                            setTranslationPrimaryKey(list, url, id)
                        else if (listFromLocal!!.isEmpty())
                            empty.value = listFromLocal.isEmpty()
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        empty.value = listFromLocal!!.isEmpty()
                    }
                })
        )*/
    }


    fun search(txt: String) {
        if (txt.isEmpty()) {
            brands.value = auxList
        } else {
            var filtered = auxList
            filtered = filtered.let { brands ->
                brands.filter {
                    BrandSimplifier(it).name?.toLowerCase()?.contains(txt.toLowerCase())!!
                }
            }
            brands.value = filtered
            empty.value = filtered.isEmpty()
        }
    }


    fun setTranslationPrimaryKey(list: List<Brand>, url:String, id:String) {
        local.save(list) {
            if (it && list.isNotEmpty()){
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            val brandList = local.getAllByString<Brand>("category.id", id)
            brandList?.let {
                brands.value = it.filter { it.isEnabled==true }.sortedByDescending { it.position }
                auxList = it.filter { it.isEnabled==true }.sortedByDescending { it.position }
            }
            error.value = ""
            empty.value=brandList?.isEmpty()
        }
    }
    fun getCategoryBySlug(id: String,  action: (cat: Category) -> (Unit)){
        loading.value = true
        val url = "$CATEGORIES_WITH_SLUG$id"
        disposable.add(
            service.getCatBySlug(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Category>>() {
                    override fun onSuccess(list: List<Category>) {
                        loading.value = false
                        if (list.isNotEmpty()){
                            action.invoke(list[0])
                            // model.value = list[0]
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


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}