package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Favorite
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults
import okhttp3.ResponseBody
import java.util.ArrayList

class FavoriteVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val favorites = MutableLiveData<List<Favorite>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    val refresh = MutableLiveData<Boolean>()
    val TAG="FavoriteVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConnectionError = MutableLiveData<Boolean>(false)


     fun getFavorites() {
        loading.value = true
        empty.value = false
        val listFromLocal =
            prefs.string(USER_ID)?.let { local.getAllByString<Favorite>("customerId", it) }
                ?.sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
        if (listFromLocal != null && listFromLocal.isNotEmpty()) {
            loading.value = false
            error.value = ""
            listFromLocal.let{
                favorites.value = it
            }

         }else{
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                empty.value = true
                return
            }
         }

      val url = "$FAVORITE_MODELS?_page=0&itemsPerPage=999&customerId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getFavoriteModel(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<Favorite>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<Favorite>>) {
                        loading.value = false
                        obj.Result?.let { list ->
                            if (list.isNotEmpty())
                                saveList(list)
                            else if (listFromLocal!!.isEmpty())
                                empty.value = listFromLocal.isEmpty()
                        }?: kotlin.run {
                            empty.value = listFromLocal?.isEmpty()
                        }

                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        empty.value = listFromLocal!!.isEmpty()
                    }
                })
        )
    }
    private fun deleteFavorite(favorite: Favorite) {
        local.realm.executeTransaction { realm ->
            val result: RealmResults<Favorite> =
                realm.where(Favorite::class.java).equalTo("id", favorite.id).findAll()
            result.deleteAllFromRealm()
            refresh.value = true
        }
    }



    fun removeFromFavoriteList(favorite: Favorite) {
        if (prefs.existKey(USER_ID)) {
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                return
            }
            loading.value = true
            val url = "$FAVORITE_MODELS/${favorite.id}"
           // deleteFavorite(favorite)
            disposable.add(
                service.deleteFavoriteModel(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                        override fun onSuccess(t: ResponseBody) {
                            Log.e(TAG, "onSuccess: ${t.string()}")
                            loading.value = false
                            deleteFavorite(favorite)
                            //1682a75a-3fd5-4418-9993-e7b7c546eca0
                            //favorite-client-model-datas/9a1e792b-0914-4c36-887e-d221af84ab3f
                        }

                        override fun onError(e: Throwable) {
                            //loading.value = false
                            Log.e(TAG, "onError: ${e.localizedMessage}")
                            deleteFavorite(favorite)
                        }

                    })
            )
        }else{
            deleteFavorite(favorite)
        }
    }
    fun saveList(list: List<Favorite>) {
        local.save(list) {
            favorites.value =
                prefs.string(USER_ID)?.let { local.getAllByString<Favorite>("client.id", it) }
                    ?.sortedByDescending {
                        it.createdAt?.let { it1 ->
                            Utils.instance.stringToDate(
                                it1, "yyyy-MM-dd'T'HH:mm:ss"
                            )?.time
                        }
                    }
            error.value = ""
            //empty.value = favorites.value?.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}