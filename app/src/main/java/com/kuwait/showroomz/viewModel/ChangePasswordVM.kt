package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.ChangePasswordRequest
import com.kuwait.showroomz.model.simplifier.LoginResponse
import com.kuwait.showroomz.model.simplifier.ResetPasswordRequestWithOtp
import com.kuwait.showroomz.model.simplifier.ResetPasswordResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class ChangePasswordVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<Boolean>(false)
    val noConnectionError = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)

    val email: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val oldPassword: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val newPassword: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val confirmPassword: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val validOldPassword: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validNewPassword: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validConfirmPassword: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }

    fun changePassword() {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            loading.value = false
            return
        }
        if (email.value == null) {
            validOldPassword.value = !oldPassword.value.isNullOrEmpty()
            validNewPassword.value = !newPassword.value.isNullOrEmpty()
            validConfirmPassword.value =
                !confirmPassword.value.isNullOrEmpty() && confirmPassword.value == newPassword.value
            if (validOldPassword.value == true && validNewPassword.value == true && validConfirmPassword.value == true) {
                loading.value = true
                disposable.add(
                    service.changePassword(prefs.string(TOKEN), ChangePasswordRequest(prefs.string(
                        USER_ID
                    )?.let { local.getOne<User>(it)?.email },
                        oldPassword.value.toString(),
                        newPassword.value.toString()
                    )
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                            override fun onSuccess(obj: BaseResponse<User>) {
                                //Log.e("ChangePasswordVM", "onSuccess: "+t.message )
                                success.value = true
                                loading.value = false
                            }

                            override fun onError(e: Throwable) {
                                Log.e("ChangePasswordVM", "onError: " + e.message)
                                error.value = true
                                loading.value = false
                            }

                        })
                )
            }
        }else{
            resetPassword()
        }
    }

    fun resetPassword() {

        validOldPassword.value = !oldPassword.value.isNullOrEmpty()
        validNewPassword.value = !newPassword.value.isNullOrEmpty()
        validConfirmPassword.value = !confirmPassword.value.isNullOrEmpty() && confirmPassword.value == newPassword.value

        if ( validOldPassword.value == true && validNewPassword.value == true && validConfirmPassword.value == true){
            loading.value=true
            disposable.add(
                service.changePassword(prefs.string(TOKEN),
                    ResetPasswordRequestWithOtp(email.value, newPassword.value.toString(),oldPassword.value.toString() )
                )
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                        override fun onSuccess(obj: BaseResponse<User>) {
                            //Log.e("ChangePasswordVM", "onSuccess: "+t.message )
                            success.value=true
                            loading.value=false
                        }

                        override fun onError(e: Throwable) {
                            Log.e("ChangePasswordVM", "onError: "+e.message )
                            error.value=true
                            loading.value=false
                        }

                    })
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }


}