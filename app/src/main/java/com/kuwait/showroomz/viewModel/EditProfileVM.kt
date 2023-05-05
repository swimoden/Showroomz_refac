package com.kuwait.showroomz.viewModel

import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.BaseResponse
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.UploadFileResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlin.text.isNullOrEmpty


class EditProfileVM : ViewModel() {

    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val noConnectionError = MutableLiveData<Boolean>(false)
    val successUploadFile = MutableLiveData<Boolean>(false)
    val email: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val gender: MediatorLiveData<Int> by lazy { MediatorLiveData<Int>() }
    val civilNumber: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val dateOfBirth = ObservableField<String>()
    val name: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val phone: MediatorLiveData<String> by lazy { MediatorLiveData<String>() }
    val validEmail: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validCivilNumber = MutableLiveData<Boolean>(true)
    val validName: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validPhone: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    val validGender: MediatorLiveData<Boolean> by lazy { MediatorLiveData<Boolean>() }
    lateinit var user: User
    var userImage: String? = null
    var userLicence: String? = null
    var userImagePath: Uri? = null
    var userLicencePath: Uri? = null
    val selectedDateOfBirth = ObservableField<String>()
    val verifyPhone = MutableLiveData<Boolean>(false)
    fun getUser() {

        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it

                    email.value = user.email
                    name.value = user.fullName
                    phone.value = user.phone?.number
                    civilNumber.value = user.civilID
                    gender.value = user.gender
                    if (user.dateOfBirth != null) {
//            selectedDateOfBirth.set(user.dateOfBirth)
                        dateOfBirth.set(
                            Utils.instance.dateToString(
                                Utils.instance.stringToDate(
                                    user.dateOfBirth ?: "",
                                    "yyyy-MM-dd'T'HH:mm:ss"
                                )!!, "MMM d, ''yy"
                            )
                        )

                        selectedDateOfBirth.set(user.dateOfBirth)
                    }
                    userImage =
                        if (user.image != null) "${BuildConfig.API_VERSION}$IMAGES/${user.image?.id}" else null
                    userLicence =
                        if (user.driversLicense != null) "${BuildConfig.API_VERSION}$IMAGES/${user.driversLicense?.id}" else null
                }
            }
        }

    }

    fun uploadImage(path: String?, from: String) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        val file = File(path)
        loading.value = true
        val requestFile: RequestBody = RequestBody.create(
            "image/png".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        disposable.add(
            service.uploadImage(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<UploadFileResponse>() {
                    override fun onSuccess(t: UploadFileResponse) {
                        if (t.data?.id != "") {
                            loading.value = false
                            userImage = t.data?.id.toString()

                        }

                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        Log.e("EditProfileVM", "onError: ${e.localizedMessage} ")
                    }

                })

        )

    }

    fun uploadMedia(path: String, from: String) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        loading.value = true
        val file = File(path)
        val requestFile: RequestBody = RequestBody.create(
            "image/png".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        disposable.add(
            service.uploadImage(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<UploadFileResponse>() {
                    override fun onSuccess(t: UploadFileResponse) {
                        if (t.data?.id != "") {
                            loading.value = false
                            successUploadFile.value = true
                            userLicence = t.data?.id
                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        Log.e("uploadMedia", "onError: ${e.localizedMessage} ")
                    }

                })

        )

    }

    fun updateProfile() {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }

        if (name.value.isNullOrEmpty())
            validName.value = false
        else
            validName.value = name.value?.validateLetters()!!

        validEmail.value = Utils.instance.isValidEmail(email.value)

        if (phone.value.isNullOrEmpty())
            validPhone.value = false
        else
            validPhone.value = phone.value?.isValidPhoneNumber()!!

        if (civilNumber.value.toString().isNotEmpty())
            validCivilNumber.value = civilNumber.value.toString().length == 12

        if (gender.value != null) {
            validGender.value = gender.value!! > -1
        }

        if (validName.value == true && validEmail.value == true && validCivilNumber.value == true && validPhone.value == true) {
            /*if (user.phone?.number != phone.value) {
                if (verifyPhone.value == false) {
                    verifyPhone.value = true
                    return
                }
            }*/
            loading.value = true
            var json = JsonObject()
            var phoneJson = JsonObject()
            phoneJson.addProperty("code", "+965")
            phoneJson.addProperty("number", phone.value)
            json.add("phone", phoneJson)
            json.addProperty("civilID", civilNumber.value)
            json.addProperty("fullName", name.value)
            json.addProperty("gender", gender.value)
            json.addProperty("email", email.value)
            json.addProperty("dateOfBirth", selectedDateOfBirth.get())

            if (userImage?.isNotEmpty() == true)
                json.addProperty("image", userImage)
            if (userLicence?.isNotEmpty() == true)
                json.addProperty("driversLicense", userLicence)

            disposable.add(
                service.updateUserInformation(prefs.string(USER_ID)!!, json)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                        override fun onSuccess(obj: BaseResponse<User>) {
                            obj.Result?.let {
                                local.saveObject(it) {}
                                success.value = true
                            }?: kotlin.run {
                                success.value = false
                                error.value = "login_error"
                            }
                            loading.value = false

                        }
                        override fun onError(e: Throwable) {
                            loading.value = false
                            Log.e("onErrore", "onError: ${e.localizedMessage} ")
                            error.value = "login_error"
                            verifyPhone.value = false
                        }
                    }
                )
            )
        }
    }
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}