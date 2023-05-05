package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.NetworkUtils
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.TOKEN
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.LoginRequest
import com.kuwait.showroomz.model.simplifier.LoginResponse
import com.kuwait.showroomz.model.simplifier.ResetPasswordRequest
import com.kuwait.showroomz.model.simplifier.ResetPasswordResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class ResetPasswordVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val email: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val password: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val validEmail: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val noConectionError = MutableLiveData<Boolean>( false)

    fun resetPassword() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        }else {
            validEmail.value = Utils.instance.isValidEmail(email.value)

            if (validEmail.value == true) {
                loading.value = true
                disposable.add(
                    service.resetPassword(ResetPasswordRequest(email.value!!))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<ResetPasswordResponse>() {
                            override fun onSuccess(responseBody: ResetPasswordResponse) {
                                Log.e("ResetPasswordVM", "onSuccess: ${responseBody.message}")
                                loading.value = false
                                success.value = responseBody.Success
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                error.value = "reset_password_error" }
                        })
                )
            }
        }
    }


}