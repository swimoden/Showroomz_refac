package com.kuwait.showroomz.model.repository

import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.CategorySimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

object LogProgressRepository {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    fun logProgress(
        screenName: String,
        category: String = "",
        dealerData: String = "",
        modelData: String = "",
        trim: String = "",
        ads: String = "",
        bank: String = ""
    ) {
        /*var url =
            API_URL + DEVICE_ACTIVITY_LOG_PROGRESS + "?serialNumber=${DeviceManger.getDeviceId()}" + "&screenName=${screenName}"
        if (category != "") url += "&category=${category}"
        if (dealerData != "") url += "&dealerData=${dealerData}"
        if (modelData != "") url += "&modelData=${modelData}"
        if (trim != "") url += "&trim=${trim}"
        if (ads != "") url += "&advertisement=${ads}"
        if (bank != "") url += "&bank=${bank}"*/
        // url += "&_locale=${if (isEnglish)"en" else "ar"}"
        /*disposable.add(
            service.getDeviceActivityLogProgress(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Log.e("logProgress", "onSuccess: ${t.string()}")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("logProgress", "onError: ${e.localizedMessage}")
                    }
                }
                )
        )*/

        Firebase.analytics.logEvent(screenName) {
            param("serialNumber", "${DeviceManger.getDeviceId()}")
            param("device_OS", "android")

            if (category != "") {
                local.getOne<Category>(id = category)?.let {
                    it.translations?.en?.name?.let { it1 -> param("category", it1) }
                }
            }

            if (dealerData != "") {
                local.getOne<Brand>(id = dealerData)?.let {
                    it.dealer?.let { it1 ->
                        local.getOne<BrandStock>(it1.toString())?.let {
                            it.translations?.en?.name?.let { it2 -> param("dealer", it2) }
                        }
                    }
                }
            }

            if (modelData != "") {
                local.getOne<Model>(id = modelData)?.let {
                    it.model?.id?.let { it1 ->
                        local.getOne<ModelStock>(it1)?.let {
                            it.translations?.en?.name?.let { it2 -> param("model", it2) }
                        }
                    }
                }
            }

            if (trim != "") {
                local.getOne<Trim>(id = trim)?.let {
                    it.translations?.en?.name?.let { it1 -> param("trim", it1) }
                }
            }

            if (ads != "") {
                local.getOne<Advertisement>(id = ads)?.let {
                    it.translations?.en?.name?.let { it1 -> param("ads", it1) }
                }
            }

            if (bank != "") {
                local.getOne<Bank>(id = bank)?.let {
                    it.translations?.en?.name?.let { it1 -> param("bank", it1) }
                }
            }
        }
    }

    fun registerDeviceId(id: String) {
        val request = JsonObject()
        request.addProperty("deviceId", id)
        disposable.add(
            service.registerDevice(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Log.e("device register", "onSuccess: ${t.string()}")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("device register", "onError: ${e.localizedMessage}")
                    }
                }
                )
        )
    }

    fun refreshMainUserData() {
        getCallBacks()
    }

    private fun getCallBacks(page: Int = 1) {
        val url = "$GET_CALLBACKS?_page=$page&itemsPerPage=5&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getCallbacks(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Callback>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Callback>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                local.save(list) {}
                                getCallBacks(page + 1)
                            } else {
                                getFinanceCallBacks()
                            }
                        }

                    }

                    override fun onError(e: Throwable) {
                        getFinanceCallBacks()
                    }
                })
        )
    }

    private fun getFinanceCallBacks(page: Int = 1) {
        val url = "$CALLBACK_BANKS?_page=$page&itemsPerPage=5&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getCallbacks(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Callback>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Callback>>) {
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                local.save(list) {}
                                getFinanceCallBacks(page + 1)
                            } else {
                                getTestDrives()
                            }
                        } ?: kotlin.run {
                            getTestDrives()
                        }

                    }

                    override fun onError(e: Throwable) {
                        getTestDrives()
                    }
                })
        )
    }

    private fun getTestDrives(page: Int = 1) {
        val url = "$TEST_DRIVE?_page=$page&itemsPerPage=5&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getTestDrive(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<TestDrive>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<TestDrive>>) {
                        obj.Result?.let { list ->
                            local.save(list) {}
                            if( list.isNotEmpty())
                            getTestDrives(page + 1)
                        }
                    }
                    override fun onError(e: Throwable) { }
                })
        )
    }

    fun incrementNbView(ads: Advertisement) {
        disposable.add(
            service.incrementAdsNbView(ads)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<IncrementNViewResponse>() {
                    override fun onSuccess(t: IncrementNViewResponse) {
                        print(t)
                    }

                    override fun onError(e: Throwable) {
                        print(e)
                    }

                })
        )
    }
}