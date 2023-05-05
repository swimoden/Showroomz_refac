package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Booking
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.local.LocalRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class PaymentListVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val payments = MutableLiveData<List<Booking>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    private val local = LocalRepo()
    private val prefs = Shared()
    val noConectionError = MutableLiveData<Boolean>( false)

     fun getPayment() {
        loading.value = true
        empty.value = false
         val  listFromLocal = prefs.string(USER_ID)?.let{
             local.getAllByString<Booking>("clientId", it)
                ?.sortedByDescending {
                    it.createdAt?.let { it1 ->
                        Utils.instance.stringToDate(
                            it1, "yyyy-MM-dd'T'HH:mm:ss"
                        )?.time
                    }
                }
        }
         if (listFromLocal != null && listFromLocal.isNotEmpty()) {
             loading.value = false
             error.value = ""
             listFromLocal.let{
                 payments.value = it
             }

         }else{
             if (!NetworkUtils.instance.connected) {
                 loading.value = false
                 empty.value = true
                 noConectionError.value = true
                 return
             }
         }


        val url = "$GET_PAYMENTS?_page=0&itemsPerPage=999&clientId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getPayments(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Booking>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Booking>>) {
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
                        empty.value = listFromLocal!!.isEmpty()
                    }
                })
        )
    }

    fun saveList(list: List<Booking>) {
        local.save(list) {
            Log.e("PaymentListVM", "saveList:$it " )
            payments.value =
                local.getAllByString<Booking>("clientId", prefs.string(USER_ID)!!)
                    ?.sortedByDescending {
                        it.createdAt?.let { it1 ->
                            Utils.instance.stringToDate(
                                it1, "yyyy-MM-dd'T'HH:mm:ss"
                            )?.time
                        }
                    }
            error.value = ""
            empty.value = payments.value?.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}