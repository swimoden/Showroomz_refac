package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.FavoriteRepository
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ModelOffersVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val model = MutableLiveData<Model>()
    val images = MutableLiveData<List<Image>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val trimsLoading = MutableLiveData<Boolean>(false)
    val trims = MutableLiveData<TrimResponse>()
    val successCallback = MutableLiveData<Boolean>(false)
    var civilId = ObservableField<String>()
    var civilIdError = MutableLiveData<Boolean>(false)
    private val TAG = "ModelOffersVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    lateinit var user: User
    var fromBuyNow: Boolean = false
    var selectOffer :Offer? =null
    var callbackError = MutableLiveData<Boolean>(false)
    var callbackLoading = MutableLiveData<Boolean>(false)
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var isCivilIdMandatory = ObservableBoolean(false)
    lateinit var modelId: String
    val noConectionError = MutableLiveData<Boolean>( false)
    val verifyPhone = MutableLiveData<Boolean>(false)
    var offers :ArrayList<Offer> = arrayListOf()
    init {
        if (prefs.existKey(USER_ID)) {
             prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it
                    fullNameField.set(user.fullName)
                    phoneNumber.set(user.phone?.number)
                    civilId.set(user.civilID)
                }
             }

        }

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun getTrims(usedFor: String, modelId: String) {
        model.value?.let {
            ModelSimplifier(it).brand?.cat?.let{
                it.isCivilIdMandatory?.let {
                    isCivilIdMandatory.set(
                        it
                    )
                }
            }
        }

        val trimsFromLocal = local.getOne<TrimResponse>(modelId)
        if (!trimsFromLocal.isNull()) {
            trimsFromLocal?.let {
                trims.value = it
            }

        } else {
            if (!NetworkUtils.instance.connected) {
                trimsLoading.value = false
                //trims.value = null
                noConectionError.value = true
                return
            }
            val url = "model-data/$modelId/$TRIMS_OFFERS"
            trimsLoading.value = true
            disposable.add(
                service.getModelTrims(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<TrimResponse>() {
                        override fun onSuccess(response: TrimResponse) {
                            Log.e("RESPONSE", response.toString())
                            trimsLoading.value = false
                            if (!response.isNull())
                                saveTrims(response, modelId, url)
                        }

                        override fun onError(e: Throwable) {
                            Log.e("Throwable", e.localizedMessage)
                            trimsLoading.value = false
                        }
                    })
            )
        }

    }

    private fun saveTrims(response: TrimResponse, id: String, url: String) {
        local.saveObject(response) {
            trims.value = local.getOne(id)
        }
    }
    fun requestCallback() {
        if (!NetworkUtils.instance.connected) {
            loading.value = false
            noConectionError.value = true
            return
        }

        if (fullNameField.get().isNullOrEmpty()) {
            nameError.value = true
            return
        } else
            nameError.value = false
        //if (phoneNumber.get().isNullOrEmpty() || phoneNumber.get()?.length != 8) {
        if (phoneNumber.get()?.isValidPhoneNumber() == true) {
            phoneError.value = true
            return
        } else
            phoneError.value = false

        if (isCivilIdMandatory.get()) {
            if (civilId.get().toString().length != 12) {
                civilIdError.value = true
                return
            } else
                civilIdError.value = false

        }
       /* if (!prefs.existKey(USER_ID) && verifyPhone.value == false) {
            verifyPhone.value = true
            return
        }*/


        if (nameError.value == false && phoneError.value == false && civilIdError.value == false) {
            val json = JsonObject()
            val phoneJson = JsonObject()
            phoneJson.addProperty("code", "+965")
            phoneJson.addProperty("number", phoneNumber.get())
            json.addProperty("fullName", fullNameField.get())
            json.add("phone", phoneJson)
            json.addProperty("modelData", modelId)
            if (prefs.existKey(USER_ID)) {
                json.addProperty("client", prefs.string(USER_ID))
            }
            if (isCivilIdMandatory.get()) {
                json.addProperty("civilId", civilId.get())
            }
            json.addProperty("email", "")
            
            callbackLoading.value = true
            disposable.add(
                service.sendCallback(json)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<Callback>>() {
                        override fun onSuccess(t: BaseResponse<Callback>) {
                            if (!prefs.existKey(USER_ID)) {
                                t.Result?.client?.id?.let {
                                    if(it != "") {
                                        prefs.setString(USER_ID, it)
                                        prefs.setString(TOKEN, it)
                                    }
                                    LogProgressRepository.refreshMainUserData()
                                }
                            }
                            t.Result?.let { saveCallback(it) }

                            successCallback.value = true
                            callbackLoading.value = false
                        }

                        override fun onError(e: Throwable) {
                            e.let { it.message?.let { it1 -> Log.e("RESPONSE_ERROR", it1) } }
                            callbackLoading.value = false
                            callbackError.value = true
                            verifyPhone.value = false
//                            nameError.set(true)
//                            phoneError.set(true)
                        }

                    })
            )
        }
    }

    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            Log.e(TAG, "saveCallback: $it")
        }
    }
}

