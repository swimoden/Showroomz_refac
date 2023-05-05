package com.kuwait.showroomz.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlin.text.isNullOrEmpty

class FinanceRequestVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val callbacks = MutableLiveData<List<Callback>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    lateinit var auxList: List<Brand>
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConnectionError = MutableLiveData<Boolean>(false)

     fun fetchCallbacks() {
        loading.value = true
        empty.value = false

        val listFromLocal =
            local.getAll<Callback>()?.filter { callback -> callback.discr == "callback_bank"&&callback.modelData?.id != null &&!callback.bankAccountNumber.isNullOrEmpty() &&callback.client?.id==prefs.string(USER_ID)}?.sortedByDescending { it.createdAt?.let { it1 ->
                Utils.instance.stringToDate(
                    it1,"yyyy-MM-dd'T'HH:mm:ss")?.time
            } }
        if (listFromLocal != null && listFromLocal.isNotEmpty()) {
            loading.value = false
            error.value = ""
            listFromLocal.let{
                callbacks.value = it
            }
        }else{
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                empty.value = true
                return
            }
        }

        val url = "$CALLBACK_BANKS?_page=0&itemsPerPage=999&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getCallbacks(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Callback>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Callback>>) {
                        loading.value = false
                        obj.Result?.let{ list ->
                            if (list.isNotEmpty())
                                saveList(list)
                            else if (listFromLocal!!.isEmpty())
                                empty.value = listFromLocal.isEmpty()
                        }?: kotlin.run {
                            empty.value = true
                        }

                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        empty.value = listFromLocal!!.isEmpty()
                    }
                })
        )
    }

    fun saveList(list: List<Callback>) {
        local.save(list) {
            callbacks.value =
                local.getAll<Callback>()?.filter { callback -> callback.discr == "callback_bank"&&callback.modelData?.id != null&&!callback.bankAccountNumber.isNullOrEmpty() &&callback.client?.id==prefs.string(USER_ID) }?.sortedByDescending { it.createdAt?.let { it1 ->
                    Utils.instance.stringToDate(
                        it1,"yyyy-MM-dd'T'HH:mm:ss")?.time
                } }
            error.value = ""
            empty.value = callbacks.value?.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}