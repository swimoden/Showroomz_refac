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
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BankSimplifier
import com.kuwait.showroomz.model.simplifier.CallbackFinanceRequest
import com.kuwait.showroomz.model.simplifier.CallbackFinanceWithUserRequest
import com.kuwait.showroomz.model.simplifier.CallbackResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class FinanceVM : ViewModel() {
    private val local = LocalRepo()
    private val TAG = "FinanceVM"
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var civilId = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var civilIdError = MutableLiveData<Boolean>(false)
    lateinit var modelId: String
    lateinit var bankId: String
    lateinit var totalCost: String
    lateinit var loanAmount: String
    lateinit var downpayment: String
    lateinit var installmentPeriod: String
    lateinit var installmentAmount: String
    lateinit var profite: String
    val prefs = Shared()
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var banks = MutableLiveData<List<Bank>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val successCallback = MutableLiveData<Boolean>(false)
    var callbackLoading = MutableLiveData<Boolean>(false)
    var callbackError = MutableLiveData<Boolean>(false)
    var isCivilIdMandatory = ObservableBoolean(false)
    var isKfh = ObservableBoolean(false)
    var acceptCondition = ObservableBoolean(true)
    var acceptConditionError = MutableLiveData<Boolean>(false)
    fun isConnected() = prefs.existKey(USER_ID)
    lateinit var user: User
    fun isValidModel() = ::modelId.isInitialized
    fun isRatioSelected() = ::installmentPeriod.isInitialized
    val noConnectionError = MutableLiveData<Boolean>(false)
    val verifyPhone = MutableLiveData<Boolean>(false)
    init {
        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                  local.getOne<User>(it)?.let{
                      user = it
                      fullNameField.set(user.fullName)
                      phoneNumber.set(user.phone?.number)
                      civilId.set(user.civilID)
                }
            }

        }
    }

    fun getBanks() {
        val auxBank = local.getAll<Bank>()
       if (isKfh.get()){
           auxBank?.let { b ->
               banks.value = b.filter { BankSimplifier(it).name=="KFH Bank (Islamic)" || BankSimplifier(it).name=="بيت التمويل الكويتي (إسلامي)"}
           }
       }else {
           auxBank?.let { b ->
               banks.value = b.filter { it.isPartner == true  }
           }
       }
        if (banks.value?.size == 0) {
                if (!NetworkUtils.instance.connected) {
                    loading.value = false
                    return
                }

            disposable.add(
                service.getBanks()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Bank>>>() {
                        override fun onSuccess(obj: BaseListResponse<List<Bank>>) {
                            obj.Result?.let { t ->
                                if (t.isNotEmpty()){
                                    saveBanks(t)
                                }
                            }

                        }


                        override fun onError(e: Throwable) {
                            error.value = e.localizedMessage
                            loading.value = false
                        }

                    })
            )
        }


    }

    private fun saveBanks(t: List<Bank>) {
        local.save(t) {
            loading.value = false
            if (isKfh.get()){
                banks.value = t.filter { BankSimplifier(it).name=="KFH Bank (Islamic)"||BankSimplifier(it).name=="بيت التمويل الكويتي (إسلامي)"}
            }else
            banks.value = t.filter { it.isPartner!! }
        }
    }

    fun requestCallback() {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            loading.value = false
            return
        }
        if (fullNameField.get().isNullOrEmpty()) {
            nameError.value = true
            return
        } else
            nameError.value = false
        //if (phoneNumber.get().isNullOrEmpty() || phoneNumber.get()?.length != 8) {
        if (phoneNumber.get()?.isValidPhoneNumber() == true) {
            phoneError.value = false
        } else {
            phoneError.value = true
            return
        }

        if ( isCivilIdMandatory.get() || !civilId.get().isNullOrEmpty()) {
            if (civilId.get().toString().length != 12) {
                civilIdError.value = true
                return
            } else
                civilIdError.value = false
            if (isKfh.get()) {
                if (!acceptCondition.get()) {
                    acceptConditionError.value = true
                    return
                } else acceptConditionError.value = false
            }
        }

       /* if (!isConnected() && verifyPhone.value == false) {
            verifyPhone.value = true
            return
        }*/
        if (nameError.value == false && phoneError.value == false && civilIdError.value == false) {
           /* var callback = CallbackFinanceRequest(
                fullName = fullNameField.get().toString(),
                phone = Phone("+965", phoneNumber.get().toString()),
                loanAmount = loanAmount,
                totalCost = totalCost,
                downpayment = downpayment,
                installmentPeriod = installmentPeriod,
                installmentAmount = installmentAmount,
                profit = profite,
                isKFH = isKfh.get()
            )

            if (prefs.existKey(USER_ID)) {
                callback = CallbackFinanceWithUserRequest(
                    fullName = fullNameField.get().toString(),
                    phone = Phone("+965", phoneNumber.get().toString()),
                    loanAmount = loanAmount,
                    totalCost = totalCost,
                    downpayment = downpayment,
                    installmentPeriod = installmentPeriod,
                    installmentAmount = installmentAmount,
                    profit = profite
                )
                callback.isKFH = isKfh.get()
                callback.client =  prefs.string(USER_ID)
            }
            if (!civilId.get().isNullOrEmpty()) {
                callback.civilId = civilId.get().toString()
            }
            if (::modelId.isInitialized)
                callback.modelData = modelId
            else callback.modelData = null
            callback.bank = bankId*/


            val json = JsonObject()
            val phoneJson = JsonObject()
            phoneJson.addProperty("code", "+965")
            phoneJson.addProperty("number", phoneNumber.get())
            json.addProperty("fullName", fullNameField.get())
            json.add("phone", phoneJson)
            if (::modelId.isInitialized)
            json.addProperty("modelData", modelId)
            if (prefs.existKey(USER_ID)) {
                json.addProperty("client", prefs.string(USER_ID))
            }
            if (isCivilIdMandatory.get()) {
                json.addProperty("civilId", civilId.get())
            }

            json.addProperty("isKFH", isKfh.get())
            json.addProperty("loanAmount", loanAmount)
            json.addProperty("totalCost", totalCost)
            json.addProperty("downpayment", downpayment)
            json.addProperty("installmentPeriod", installmentPeriod)
            json.addProperty("installmentAmount", installmentAmount)
            json.addProperty("profit", profite)
            json.addProperty("bank", bankId)


            callbackLoading.value = true
            disposable.add(
                service.sendFinanceCallback(json)
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
                        }
                    })
            )
        }
    }

    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            t.client?.id?.let { it1 ->
                prefs.setString(USER_ID, it1)
            }
            Log.e(TAG, "saveCallback: $it")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}