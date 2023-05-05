package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.context
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.simplifier.CallbackResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class SMSVerificationVM : ViewModel(){
    private val disposable = CompositeDisposable()
    private val service = ApiService()
    val loading =  MutableLiveData<Boolean>(false)
    val successSentCode = MutableLiveData<Boolean>(false)
    val successVerifyCode = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<Boolean>(false)
    val errorMsg = MutableLiveData<String>()
    val successMsg = MutableLiveData<String>()
    val noConnectionError = MutableLiveData<Boolean>(false)

    fun sendCode(phone: String) {
        loading.value = true
        val url = "$PHONECODEREQUEST?code=%2B965&number=$phone"
        disposable.add(
            service.getCode(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallbackResponse>() {
                    override fun onSuccess(obj: CallbackResponse) {
                        loading.value = false

                        if (obj.status == "success") {

                            obj.message?.let {
                                successMsg.value = context.getStringS(it) }
                                ?: run{
                                    successMsg.value = "success"
                            }
                            successSentCode.value = true
                        } else {

                            obj.message?.let {
                                errorMsg.value = context.getStringS(it) }
                                ?: run{
                                    errorMsg.value = "success"
                                }
                            error.value = true

                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        errorMsg.value = context.getString(R.string.error_occurred)
                        try {
                            if ((e as HttpException).response()?.code() == 400) {
                                val x = e.response()?.errorBody()?.string()
                                    ?.toString()
                                x?.jsonObject?.let {
                                    val msg = it.get("message")
                                    errorMsg.value = context.getStringS(msg.toString())
                                } ?: kotlin.run {
                                    errorMsg.value = context.getString(R.string.error_occurred)
                                }
                            }


                        } catch (e: ClassCastException) {
                            errorMsg.value = context.getString(R.string.error_occurred)
                        }
                        error.value = true
                    }
                })
        )
    }

    fun verifyCode(phone:String,verificationCode:String){
        loading.value = true
        val url = "$PHONECODEVERIFY?code=%2B965&number=$phone&validation_code=$verificationCode"
        disposable.add(
            service.verifyCode(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallbackResponse>() {
                    override fun onSuccess(obj: CallbackResponse) {
                        loading.value = false
                        val s = context.resIdByName(obj.message, "string")
                        if (obj.status == "success") {
                            successMsg.value = context.getString(s)
                            successVerifyCode.value = true
                        } else {
                            errorMsg.value = context.getString(s)
                            error.value = true
                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        errorMsg.value = context.getString(R.string.error_occurred)
                        try {
                             if ((e as HttpException).response()?.code() == 400) {
                                val x = e.response()?.errorBody()?.string()
                                    ?.toString()
                                x?.jsonObject?.let {
                                    val msg = it.get("message")
                                    errorMsg.value = context.getStringS(msg.toString())
                                } ?: kotlin.run {
                                    errorMsg.value = context.getString(R.string.error_occurred)
                                }
                            }


                        } catch (e: ClassCastException) {
                            errorMsg.value = context.getString(R.string.error_occurred)
                        }
                        error.value = true


                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }


}