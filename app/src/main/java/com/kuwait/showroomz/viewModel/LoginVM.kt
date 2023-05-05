package com.kuwait.showroomz.viewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.FavoriteRepository
import com.kuwait.showroomz.model.simplifier.LoginRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import kotlin.text.isNullOrEmpty

class LoginVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    val prefs = Shared()
    val error = MutableLiveData<String>()
    val noConectionError = MutableLiveData<Boolean>(false)
    val updatePass = MutableLiveData<Boolean>(false)
    val callRegister = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val email: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val password: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val validEmail: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validPassword: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    var existEmail = MutableLiveData<Boolean>(false)

    fun login() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        } else {
            validEmail.value = Utils.instance.isValidEmail(email.value)
            validPassword.value = !password.value.isNullOrEmpty()
            if (validEmail.value == true && validPassword.value == true) {
                loading.value = true
                disposable.add(
                    service.login(
                        LoginRequest(
                            email.value!!,
                            password.value!!

                        )
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                            override fun onSuccess(responseBody: BaseResponse<User>) {
                                // getUserInfo(responseBody.token)
                                responseBody.Result?.let {
                                    saveUser(it)
                                    prefs.setString(TOKEN, it.id ?: "")
                                    it.id?.let {
                                        prefs.setString(TOKEN, it)
                                        prefs.setString(USER_ID, it)
                                    }
                                    loading.value = false
                                }
                            }

                            override fun onError(e: Throwable) {
                                getOtp()
                            }
                        })
                )
            }
        }
    }

    private fun getOtp() {
        disposable.add(
            service.loginOtp(
                LoginRequest(
                    email.value!!,
                    password.value!!
                )
            ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<String>>() {
                    override fun onSuccess(responseBody: BaseResponse<String>) {
                        responseBody.Result?.let {
                            loading.value = false
                            if (it == "RESET_PASSWORD_OTP_ALREADY_SENT" || it == "RESET_PASSWORD_OTP_SENT") {
                                updatePass.value = true
                            } else {
                                error.value = "login_error"
                            }
                        }
                    }
                    override fun onError(e: Throwable) {
                        loading.value = false
                        error.value = "login_error"
                    }
                })
        )
    }

    private fun getUserInfo(token: String) {
        disposable.add(
            service.getUser(token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<User>() {
                    override fun onSuccess(t: User) {
                        saveUser(t)
                        prefs.setString(TOKEN, token)
                        t.id?.let { prefs.setString(USER_ID, it) }
                        loading.value = false
                        FavoriteRepository().saveFavoritesUnregistered(t)
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                    }

                })
        )
    }

    fun loginSocial(json: JsonObject) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        } else {
            loading.value = true
            disposable.add(
                service.loginSocial(json).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                        override fun onSuccess(responseBody: BaseResponse<User>) {
                            responseBody.Result?.let {
                                saveUser(it)
                                prefs.setString(TOKEN, it.id ?: "")
                                it.id?.let {
                                    prefs.setString(TOKEN, it)
                                    prefs.setString(USER_ID, it)
                                }
                                loading.value = false
                            }
                        }

                        override fun onError(e: Throwable) {
                            try {
                                if ((e as HttpException).response()?.code() == 400) {
                                    callRegister.value = true
                                } else if (e.response()?.code() == 500) {
                                    loading.value = false
                                    error.value = "${e.response()?.code()}"
                                }
                            } catch (e: ClassCastException) {
                                loading.value = false
                                error.value = ""
                            }
                        }
                    })
            )
        }
    }

    fun registerSocial(json: JsonObject) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
        } else {
            loading.value = true
            disposable.add(
                service.registerSocial(json).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                        override fun onSuccess(responseBody: BaseResponse<User>) {
                            loading.value = false
                            responseBody.Result?.let {
                                saveUser(it)
                                prefs.setString(TOKEN, it.id ?: "")
                                it.id?.let {
                                    prefs.setString(TOKEN, it)
                                    prefs.setString(USER_ID, it)
                                }
                                success.value = true
                            } ?: kotlin.run {
                                success.value = false
                            }
                        }

                        override fun onError(e: Throwable) {
                            loading.value = false
                            try {
                                if ((e as HttpException).response()?.code() == 400) {
                                    if (e.response()?.errorBody()?.string()?.contains("email")!!) {
                                        existEmail.value = true
                                    } else {
                                        error.value = "${e.response()?.code()}"
                                    }
                                } else if (e.response()?.code() == 500)
                                    error.value = "${e.response()?.code()}"
                            } catch (e: ClassCastException) {
                                error.value = ""
                            }
                        }
                    })
            )
        }
    }


    private fun saveUser(user: User) {
        var localUser = user.id?.let { local.getOne<User>(it) }
        if (localUser == null) {
            local.saveObject(user) {
                success.value = true
            }
        } else success.value = true

    }
}