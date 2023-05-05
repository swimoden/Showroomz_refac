package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.Phone
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.FavoriteRepository
import com.kuwait.showroomz.model.simplifier.RegisterRequest
import com.kuwait.showroomz.model.simplifier.RegisterResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import kotlin.text.isNullOrEmpty

class RegisterVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
     val prefs = Shared()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val email: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val password: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val name: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val phone: MediatorLiveData<String>  by lazy { MediatorLiveData<String>() }
    val validEmail: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validPassword: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validName: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validPhone: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    var existPhone = MutableLiveData<Boolean>(false)
    var existEmail= MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>( false)
    val verifyPhone = MutableLiveData<Boolean>(false)

    fun register() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        }else {
            validName.value =
                !name.value.isNullOrEmpty() && name.value?.trim()?.isNotEmpty() == true
            validEmail.value = Utils.instance.isValidEmail(email.value)
            validPassword.value =
                !password.value.isNullOrEmpty() && password.value.toString().length >= 8
            validPhone.value =
                if (phone.value?.isValidPhoneNumber() == null) false else phone.value?.isValidPhoneNumber()
           /* if (verifyPhone.value == false) {
                verifyPhone.value = true
                return
            }*/
            if (validName.value == true && validEmail.value == true && validPassword.value == true && validPhone.value == true) {
                loading.value = true
                existEmail.value = false
                existPhone.value = false
                disposable.add(
                    service.register(
                        RegisterRequest(
                            email.value.toString(),
                            name.value.toString(),
                            password.value.toString(),
                            DeviceManger.getDeviceId().toString(),
                            Phone("+965", phone.value.toString())
                        )
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                            override fun onSuccess(obj: BaseResponse<User>) {
                                obj.Result?.let { response ->
                                    saveUser(response)
                                    response.id?.let {
                                        prefs.setString(TOKEN, it)
                                        prefs.setString(USER_ID, it)
                                    }
                                }

                                loading.value = false
                                success.value = true
                                // FavoriteRepository().saveFavoritesUnregistered(response.data.user)
                            }
                               // FavoriteRepository().saveFavoritesUnregistered(response.data.user)

                            override fun onError(e: Throwable) {
                                loading.value = false
                                verifyPhone.value = false

                                try {


                                    if ((e as HttpException).response()?.code() == 400) {
                                        val x = e.response()?.errorBody()
                                        if (e.response()?.errorBody()
                                                ?.string()?.toString()?.contains("email")!!
                                        ) {

                                            existEmail.value = true
                                        } else {

                                            existPhone.value = true
                                        }
                                    }
                                    if (e.response()
                                            ?.code() == 500
                                    ) error.value = "${e.response()?.code()}"

                                } catch (e: ClassCastException) {
                                    error.value = ""

                                }

                            }
                        })
                )
            }
        }
    }

    private fun saveUser(user: User) {
        local.saveObject(user) {}
    }
}