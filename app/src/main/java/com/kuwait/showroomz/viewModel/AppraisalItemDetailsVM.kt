package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.FavoriteRepository
import com.kuwait.showroomz.model.simplifier.CallbackRequest
import com.kuwait.showroomz.model.simplifier.CallbackResponse
import com.kuwait.showroomz.model.simplifier.CallbackWithOutUserRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class AppraisalItemDetailsVM : ViewModel() {
    var request: AppraisalRequest? = null
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    val prefs = Shared()
    lateinit var user: User
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var callbackLoading = MutableLiveData<Boolean>(false)
    var callbackError = MutableLiveData<Boolean>()
    var successCallback = MutableLiveData<Boolean>()
    val noConectionError = MutableLiveData<Boolean>(false)
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var selectionError = MutableLiveData<Boolean>(false)
    private val service = ApiService()
    fun getUser() {
        if (prefs.existKey(USER_ID)) {
            user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }!!
            fullNameField.set(user.fullName)
            phoneNumber.set(user.phone?.number)

        }

    }

    fun requestCallback() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            return
        }
        if (fullNameField.get().isNullOrEmpty()) {
            nameError.value = true
            return
        } else
            nameError.value = false

        if (phoneNumber.get()?.isValidPhoneNumber() == null) {
            phoneError.value = true
            return
        } else if (!phoneNumber.get()!!.isValidPhoneNumber()) {
            phoneError.value = true
            return
        } else {
            phoneError.value = false
        }




        if (nameError.value == false && phoneError.value == false) {

            var json = JsonObject()
            json.addProperty("isAccepted", true)

            callbackLoading.value = true
            request?.id?.let {
                disposable.add(

                    service.callbackAppraisal(it,json)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<AppraisalResp>() {
                            override fun onSuccess(t: AppraisalResp) {

                                successCallback.value = t.id != null && t.id != ""
                                callbackLoading.value = false
                            }

                            override fun onError(e: Throwable) {
                                e.let { it.message?.let { it1 -> Log.e("RESPONSE_ERROR", it1) } }
                                callbackLoading.value = false
                                callbackError.value = true

                            }

                        })

                )
            }?: run{
                selectionError.value = true
            }
        }
    }

    fun fetchAppraisal(id:String, resp:(CallbackAppraisalClientVehicle?)-> Unit) {
        callbackLoading.value = true
         val localItem = local.getOne<CallbackAppraisalClientVehicle>(id)
        localItem?.let {
            callbackLoading.value = false
            resp.invoke(it)
        } ?: run{
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                callbackLoading.value = false
            }
        }

        val url = "$CALLBACK_APPRAISAL_CLIENT_VEHICLES/$id"
        disposable.add(
            service.getCallbackAppraisalClientVehicle(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallbackAppraisalClientVehicle>() {
                    override fun onSuccess(list: CallbackAppraisalClientVehicle) {
                        callbackLoading.value = false
                        resp.invoke(list)
                        local.saveObject(list){  }
                    }

                    override fun onError(e: Throwable) {
                        callbackLoading.value = false
                        resp.invoke(null)
                    }
                })
        )
    }
}