package com.kuwait.showroomz.model.repository

import android.util.Log
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Favorite
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.FavoriteParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults

class FavoriteRepository {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
     fun saveFavoritesUnregistered(t: User) {
        val favoritesWithOutUser = local.getAllByString<Favorite>("client.id","")!!
        if (favoritesWithOutUser.size>0){
            favoritesWithOutUser.forEach {
                val params = FavoriteParam("${t.id}", "${it.modelData?.id}")
                disposable.add(
                    service.postFavoriteModel(params)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<BaseResponse<Favorite>>() {
                            override fun onSuccess(obj: BaseResponse<Favorite>) {
                                obj.Result?.let {
                                    saveFavorite(it)
                                }


                            }

                            override fun onError(e: Throwable) {

                                Log.e("saveUnregistered", "onError: ${e.localizedMessage}")
                            }

                        }
                        )
                )
            }
            removeList()
        }

    }

    private fun removeList() {
        local.realm.executeTransaction { realm ->
            val result: RealmResults<Favorite> =
                realm.where(Favorite::class.java)
                    .equalTo("client.id","")
                    .findAll()

            result.deleteAllFromRealm()
        }
    }

    private fun saveFavorite(t: Favorite) {
        local.saveObject(t){
            Log.e("saveFavorite", "onError: $it")
        }
    }
}