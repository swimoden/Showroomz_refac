package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.IndustrySimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ApplyForFinanceStepTwoVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<String>()
    val haveLoan = ObservableBoolean(false)
    val loanPayment = ObservableField<String>()
    val creditCardPayment = ObservableField<String>()
    val haveCreditCard = ObservableBoolean(false)
    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>(false)
    val validCreditCardAmount = MutableLiveData<Boolean>()
    val validBank = MutableLiveData<Boolean>()
    val validLoanAmount = MutableLiveData<Boolean>()
    var selectedBank: Bank? = null
    var banks = MutableLiveData<List<Bank>>()
    val noConectionError = MutableLiveData<Boolean>(false)
    lateinit var user: User

    init {
        getUser()
        getBanks()
        haveCreditCard.set(false)
        haveLoan.set(false)
    }
    fun getUser() {

        user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }!!
    }

    fun getBanks() {
        val auxBank = local.getAll<Bank>()
        auxBank?.let { b ->
            banks.value = b.filter { it.isPartner ?: false }
        }
        if (banks.value?.size == 0) {
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
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
            banks.value = t.filter { it.isPartner ?: false }
        }
    }



    fun checkData() {
        if (selectedBank==null) {
            validBank.value = false
            return
        } else validBank.value = true

        if (haveLoan.get() && loanPayment.get() != null) {
            if (loanPayment.get()?.isEmpty() == true || loanPayment.get()?.toInt() == 0) {
                validLoanAmount.value = false
                return
            } else {
                haveLoan.set(true)
                validLoanAmount.value = true
            }
        }else{
            if (haveLoan.get() && loanPayment.get() == null){
                validLoanAmount.value = false
                return
            }else{
                validLoanAmount.value = true
            }
           // haveLoan.set(false)
            //return
        }


        if (haveCreditCard.get() && creditCardPayment.get() != null) {
            if (creditCardPayment.get()?.isEmpty() == true || creditCardPayment.get()
                    ?.toInt() == 0
            ) {
                validCreditCardAmount.value = false
                return
            } else {
                haveCreditCard.set(true)
                validCreditCardAmount.value = true
            }
        }else{
            if (haveCreditCard.get() && creditCardPayment.get() == null){
                validCreditCardAmount.value = false
                return
            }else{
                validCreditCardAmount.value = true
            }
          // haveCreditCard.set(false)
           // return
        }


        checkEligibility()


    }

    private fun checkEligibility() {
        var value = if (haveLoan.get() && loanPayment.get() != null) loanPayment.get()?.toInt() !! else 0
        value =
            value.plus((if (haveCreditCard.get()) creditCardPayment.get()?.toInt() !! else 0))
        if (value > 0) {
            var salary = user.salary?.toInt()
            val x = (if (user.isRetired == true) 0.3 else 0.4)
            val y = salary?.times(x)
            val z = (user.salary?.toInt()!! * (if (user.isRetired == true) 0.3 else 0.4))

            if ((user.salary?.toInt()!! * (if (user.isRetired == true) 0.3 else 0.4)) > value) {
                success.value = true
                CacheObjects.isEligible = true
            }else {
                error.value = "1"
                CacheObjects.isEligible = false
            }
        } else {
            CacheObjects.isEligible = true
            success.value = true
            error.value = ""
        }

    }


    private fun updateProfile() {
        var json = JsonObject()
        json.addProperty("haveCreditCard", haveCreditCard.get())
        json.addProperty("haveLoan", haveLoan.get())
        if (haveLoan.get())
            json.addProperty("monhtlyLoanPayment", loanPayment.get())
        if (haveCreditCard.get())
            json.addProperty("monhtlyLoanPayment", creditCardPayment.get())
        disposable.add(
            service.updateUserInformation(prefs.string(USER_ID)!!, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                    override fun onSuccess(obj: BaseResponse<User>) {
                        obj.Result?.let { t->
                            local.realm.executeTransaction {
                                it.copyToRealmOrUpdate(t)
                            }
//                            updateLocaleUser(t)
                            loading.value = false
                            success.value = true
                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        Log.e("onErrore", "onError: ${e.localizedMessage} ")
                       // error.value = "error"
                    }

                })
        )
    }


}