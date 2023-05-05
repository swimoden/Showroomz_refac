package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmObject
import retrofit2.HttpException

class SplashVM : ViewModel() {
    private val local = LocalRepo()
    private val prefs = Shared()
    private val service = ApiService()
    private val disposable = CompositeDisposable()


    fun getAllData() {
        fetchAllActions()
        fetchCategories()
        fetchAllBrandsStock()
        fetchAllAgenciesStock()
        fetchAllBrands()
        paginModelsStock(0)
        pagin(0)
        fetchAllBank()
    }

    private fun fetchAllBank() {
        disposable.add(
            service.getBanks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Bank>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Bank>>) {
                        obj.Result?.let { t ->
                            if (t.isNotEmpty()){
                                saveBanks(t)
                            }
                        }

                    }


                    override fun onError(e: Throwable) {
                        Log.e("fetchAllBank", e.localizedMessage)
                    }

                })
        )
    }

    private fun saveBanks(list: List<Bank>) {
        local.save(list) {
            if (list.isNotEmpty() && it) {
                val x = getCurrentDateTime()
                prefs.setString("bank_updatedAt", x.toStringDate())
            }
            Log.e("saveBanks", it.toString())
        }
    }

    private fun fetchCategories() {
        disposable.add(
            service.getCategories()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<Category>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Category>>) {
                        obj.Result?.let { list ->
                            saveToLocal(list, CATEGORIES_API)
                        }
                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                    }
                })
        )
    }

    fun setTranslationPrimaryKey(list: List<Category>) {
        local.save(list) {
            if (it && list.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("cat_updatedAt", x.toStringDate())
            }
        }
    }

    fun updateEmail(email: String, action: (success: Boolean) -> Unit) {
        //loading.value = true
        val json = JsonObject()
        json.addProperty("email", email)
        disposable.add(
            service.updateUserInformation(prefs.string(USER_ID)!!, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                    override fun onSuccess(obj: BaseResponse<User>) {
                        obj.Result?.let { t ->
                            local.saveObject(t) {}
                            action.invoke(true)
                        }
                    }
                    override fun onError(e: Throwable) {
                       action.invoke(false)
                        // emailUpdatedSuccess.value = false
                    }
                }
                )
        )
    }




    private fun getTrims(usedFor: String, modelId: String) {
        val url = "$usedFor$MODEL_DATA/$modelId/$TRIMS_OFFERS"
        disposable.add(
            service.getModelTrims(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                    override fun onSuccess(response: TrimResponse) {
                        Log.e("RESPONSE", response.toString())
                        if (!response.isNull())
                            saveTrims(response, modelId, url)
                    }

                    override fun onError(e: Throwable) {
                        Log.e("Throwable", e.localizedMessage)
                    }
                })
        )
    }

    private fun saveTrims(response: TrimResponse, id: String, url: String) {
        local.saveObject(response) {}
    }

    private fun fetchAllBrands(usedFor: String, page: Int) {

        val url = "$usedFor-datas?_page=$page&itemsPerPage=20"
        disposable.add(
            service.getBrands(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Brand>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Brand>>) {
                        obj.Result?.let { list ->
                            setBrandTranslationPrimaryKey(list, url)
                            if (list.size == 20){
                                fetchAllBrands(usedFor, page + 1)
                            }
                        }
                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }

    private fun setBrandTranslationPrimaryKey(list: List<Brand>, url: String) {
        local.save(list) {
            if (it && list.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
        }
    }

    private fun setTranslationPrimaryKey(models: List<Model>, url: String) {
        local.save(models) {
            if (it && models.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
        }
    }

    private fun saveToLocal(models: List<RealmObject>, url: String) {
        local.save(models) {
            if (it && models.isNotEmpty()) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            Log.e("saveBed", "$it --- --- $url ----- ${models.size}")
        }
    }

    private fun fetchAllActions() {

        disposable.add(
            service.getActions(ACTIONS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Action>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Action>>) {
                        obj.Result?.let {
                            saveToLocal(it, ACTIONS)
                        }


                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                    }
                })
        )
    }


    private fun fetchAllBrandsStock() {

        disposable.add(
            service.getBrandsStock(BRANDSTOCKAPI)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<BrandStock>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<BrandStock>>) {
                        obj.Result?.let {
                            saveToLocal(it, BRANDSTOCKAPI)
                        }
                    }

                    override fun onError(e: Throwable) {
                    }
                })
        )
    }

    private fun fetchAllAgenciesStock() {

        disposable.add(
            service.getBrandsStock(AGENCIESSTOCKAPI)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<BrandStock>>>() {
                    override fun onSuccess(obj:BaseListResponse<List<BrandStock>>) {
                        obj.Result?.let { list ->
                            saveToLocal(list, AGENCIESSTOCKAPI)
                        }

                        //fetchAllBrands()
                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                        //fetchAllBrands()
                    }
                })
        )
    }

    private fun fetchAllBrands() {

        //val url = "$usedFor-datas?_page=$page&itemsPerPage=20"
        disposable.add(
            service.getBrands(BRANDSLIGHT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Brand>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Brand>>) {
                        obj.Result?.let {
                            saveToLocal(it, BRANDSLIGHT)
                        }

                        // paginModelsStock(1)
                    }

                    override fun onError(e: Throwable) {
                        Log.e("Error brands", e.localizedMessage)
                        //paginModelsStock(1)
                    }
                })
        )
    }

    private fun paginModelsStock(page: Int) {
        val url = "models?_page=$page&itemsPerPage=200"

        disposable.add(
            service.getModelsStock(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<ModelStock>>>() {
                    override fun onComplete() {

                    }

                    override fun onNext(models:BaseListResponse<List<ModelStock>>) {
                        models.Result?.let {
                            if (it.size == 400) {
                                saveToLocal(it, url)
                                paginModelsStock(page + 1)
                            }else{

                            }
                        }


                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }
                        //paginModelsStock(page + 1)
                    }
                })
        )
    }

    private fun pagin(page: Int) {
        val url =
            "model-datas-light-get"//String().format(AllModelsApiLight,page)
        val json = JsonObject()
        json.addProperty("_page", page)
        json.addProperty("itemsPerPage", 200)

        disposable.add(
            service.getAllBrandModels(url, json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Model>>>() {
                    override fun onComplete() {

                    }



                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }
                        pagin(page + 1)
                    }

                    override fun onNext(t: BaseListResponse<List<Model>>) {
                        t.Result?.let { models ->
                            if (models.size == 200) {

                                pagin(page + 1)
                            }
                            saveToLocal(models, url)
                        }
                    }
                })
        )
    }


    //models?_page=%d&itemsPerPage=200


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}