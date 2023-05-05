package com.kuwait.showroomz.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class AppraisalCallbackListVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val callbackAppraisal = MutableLiveData<List<CallbackAppraisalClientVehicle>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    lateinit var auxList: List<Brand>
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConectionError = MutableLiveData<Boolean>(false)
    val infoList = MutableLiveData<List<AppraisalInfo>>()
    fun getUserCars(page: Int = 0) {

        val list = prefs.string(USER_ID)?.let {
            local.getAllByString<ClientVehicle>(
                "client.id",
                it
            )
        }

        list?.let {
            fetchAppraisalList()
        } ?: run {
            getRemoteData(page)
        }


    }

    fun getRemoteData(page: Int) {
        // viewModelScope.launch (Dispatchers.IO){
        loading.value = true
        empty.value = false
        val url =
            "client-vehicles?clientId=${prefs.string(USER_ID)}&_page=$page&itemsPerPage=5"
        disposable.add(
            service.getClientVehicleList(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<ClientVehicle>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<ClientVehicle>>) {

                        obj.Result?.let{ lista ->
                            if (lista.isNotEmpty()) {
                                local.save(lista) {
                                    val list = prefs.string(USER_ID)?.let {
                                        local.getAllByString<ClientVehicle>(
                                            "clientId",
                                            it
                                        )
                                    }

                                    getRemoteData(page + 1)
                                }
                            } else {
                                fetchAppraisalList()
                            }
                        }



                    }

                    override fun onError(e: Throwable) {
                        fetchAppraisalList()
                    }
                })
        )
        // }
    }

    fun fetchAppraisalList() {
        val listFromLocal =
            local.getAll<CallbackAppraisalClientVehicle>()
                ?.filter { callback -> callback.clientId == prefs.string(USER_ID) }
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
            listFromLocal.let {
                callbackAppraisal.value = it
            }

        } else {
            if (!NetworkUtils.instance.connected) {
                empty.value = true
                noConectionError.value = true
                loading.value = false
                return
            }
        }

        val url = "$CALLBACK_APPRAISAL_CLIENT_VEHICLES?_page=0&itemsPerPage=999&clientId=${prefs.string(USER_ID)}"

        disposable.add(
            service.getCallbackAppraisalClientVehicleList(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<CallbackAppraisalClientVehicle>>) {
                        loading.value = false
                        obj.Result?.let { list ->
                            if (list.isNotEmpty())
                                saveList(list)
                            else if (listFromLocal!!.isEmpty())
                                empty.value = listFromLocal.isEmpty()
                        }
                    }
                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        empty.value = listFromLocal?.isEmpty()
                    }
                })
        )
    }

    fun getAppraisalBrands() {
        loading.value = true
        empty.value = false
        /*val localList = local.getAll<AppraisalInfo>()

        localList?.let {
            if (it.size > 0) {
                loading.value = false
                error.value = ""
                infoList.value = it.filter { s -> s.position == 0 }
            }
        }
        if (localList?.isNotEmpty() == true) {
            local.deleteAppraisalObjects()
        }
        if (!NetworkUtils.instance.connected) {
            empty.value = true
            noConectionError.value = true
            loading.value = false
            return
        } else {*/
            disposable.add(
                service.getAppraisalInfo()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object :
                        DisposableSingleObserver<BaseListResponse<List<AppraisalInfo>>>() {
                        override fun onSuccess(obj: BaseListResponse<List<AppraisalInfo>>) {
                            loading.value = false
                            error.value = ""
                            obj.Result?.let { t ->

                                if (t.isNotEmpty()) {
                                    infoList.value = t.filter { s -> s.appraisalVehicleBrandModels.isNotEmpty()}
                                } else {
                                    empty.value = true
                                }
                                /*local.save(t) {
                                    local.getAll<AppraisalInfo>()?.let {
                                        if (it.size > 0) {
                                            infoList.value = it.filter { s -> s.appraisalVehicleBrandModels.isNotEmpty()}
                                        } else {
                                            empty.value = true
                                        }
                                    }
                                }*/
                            }
                        }

                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false
                            empty.value = false//localList?.isEmpty()
                        }
                    })
            )
      //  }

    }

    fun saveList(list: List<CallbackAppraisalClientVehicle>) {
        local.save(list) {
            callbackAppraisal.value = list.sortedByDescending {
                it.createdAt?.let { it1 ->
                    Utils.instance.stringToDate(
                        it1, "yyyy-MM-dd'T'HH:mm:ss"
                    )?.time
                }
            }

            error.value = ""
            empty.value = callbackAppraisal.value?.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}