package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.google.gson.JsonObject
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.Bank
import com.kuwait.showroomz.model.data.Callback
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.ApplyForFinanceParams
import com.kuwait.showroomz.model.simplifier.CallbackResponse
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
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

class ApplyForFinanceVM : ViewModel() {
    private val TAG = "ApplyForFinanceVM"
    var isPrivate = ObservableBoolean(true)
    val error = MutableLiveData<Boolean>(false)
    val noConnectionError = MutableLiveData<Boolean>(false)
    val loading = ObservableBoolean(false)
    lateinit var simplifier: ModelSimplifier

    //    val loading = MutableLiveData<Boolean>(false)
    val success = MutableLiveData<Boolean>()
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val successUploadNetSalaryFile = MutableLiveData<Boolean>()
    val successUploadAkaFile = MutableLiveData<Boolean>()
    val successUploadBankStatementFile = MutableLiveData<Boolean>()
    private var netSalary = ""
    private var aka = ""
    private var bankStatement = ""
    var modelId: String
    var bankId: String
    var totalCost: String
    var loanAmount: String
    var downPayment: String
    var installmentPeriod: String
    var installmentAmount: String
    var profit: String
    private val prefs = Shared()
    private val local = LocalRepo()
    var civilIdText = ObservableField<String>()
    var civilIdError = MutableLiveData<Boolean>(false)
    var bankNumberError = MutableLiveData<Boolean>(true)
    var bankStatementError = MutableLiveData<Boolean>(false)
    var akaError = MutableLiveData<Boolean>(false)
    var netSalaryError = MutableLiveData<Boolean>(false)
    var bankNumber = ObservableField<String>("")
    lateinit var user: User
    lateinit var params: ApplyForFinanceParams
    var isCivilIdMandatory = ObservableBoolean(false)
    var isKfh = ObservableBoolean(false)

    init {
        noConnectionError.value = !NetworkUtils.instance.connected
        user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }!!
        civilIdText.set(user.civilID)
        params =
            prefs.getParcelable<ApplyForFinanceParams>("APPLY_FINANCE") as ApplyForFinanceParams
        modelId = params.modelId.toString()
        bankId = params.bankId.toString()
        totalCost = params.totalCost.toString()
        loanAmount = params.loanAmount.toString()
        downPayment = params.downpayment.toString()
        installmentPeriod = params.installmentPeriod.toString()
        installmentAmount = params.installmentAmount.toString()
        profit = params.profit.toString()
        simplifier =
            local.getOne<Model>(modelId)?.let { ModelSimplifier(it) } ?: ModelSimplifier(Model())
        simplifier.category?.let {
            it.isCivilIdMandatory?.let { it1 ->
                isCivilIdMandatory.set(it1)
                isKfh.set(it.isKFh)

            }
        }

    }

    fun uploadMedia(path: String, from: String, code: Int) {

        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        loading.set(true)

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

                            if (code == 1) {
                                netSalary = t.data?.id.toString()
                                successUploadNetSalaryFile.value = true
                            }
                            if (code == 2) {
                                aka = t.data?.id.toString()
                                successUploadAkaFile.value = true
                            }
                            if (code == 3) {
                                bankStatement = t.data?.id.toString()
                                successUploadBankStatementFile.value = true
                            }

                        }
                        loading.set(false)

                    }

                    override fun onError(e: Throwable) {
                        loading.set(false)
                        Log.e("uploadMedia", "onError: ${e.localizedMessage} ")
                    }

                })

        )

    }

    fun uploadimage(path: String, from: String, code: Int) {

        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        loading.set(true)

        val file = File(path)
        val requestFile: RequestBody = RequestBody.create(
            "image/png".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        disposable.add(
            service.uploadMedia(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<UploadFileResponse>() {
                    override fun onSuccess(t: UploadFileResponse) {
                        if (t.data?.id != "") {

                            if (code == 1) {
                                netSalary = t.data?.id.toString()
                                successUploadNetSalaryFile.value = true
                            }
                            if (code == 2) {
                                aka = t.data?.id.toString()
                                successUploadAkaFile.value = true
                            }
                            if (code == 3) {
                                bankStatement = t.data?.id.toString()
                                successUploadBankStatementFile.value = true
                            }

                        }
                        loading.set(false)

                    }

                    override fun onError(e: Throwable) {
                        loading.set(false)
                        Log.e("uploadMedia", "onError: ${e.localizedMessage} ")
                    }

                })

        )

    }


    fun requestBankCallback() {

        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }
        if (bankNumber.get().toString().length != 14) {
            bankNumberError.value = false
            return
        }

        if (civilIdText.get().toString().length != 12) {
            civilIdError.value = true
            return
        }


        //if (bankNumberError.value == false &&  civilIdError.value == false) {
        val json = JsonObject()
        val phoneJson = JsonObject()
        phoneJson.addProperty("code", "+965")
        phoneJson.addProperty("number", user.phone?.number)
        json.add("phone", phoneJson)
        json.addProperty("fullName", user.fullName)
        json.addProperty("client", "${user.id}")
        if (modelId != "")
            json.addProperty("modelData", "${modelId}")
        json.addProperty("bank", bankId)
        json.addProperty("loanAmount", loanAmount)
        json.addProperty("totalCost", totalCost)
        json.addProperty("downpayment", downPayment)
        json.addProperty("installmentPeriod", installmentPeriod)
        json.addProperty("installmentAmount", installmentAmount)
        json.addProperty("profit", profit)
        if (netSalary != "")
            json.addProperty("salaryCertificate", netSalary)
        if (aka != "")
            json.addProperty("akamathopia", aka)
        if (isPrivate.get() && bankStatement != "")
            json.addProperty("monthsBankStatement", bankStatement)
        json.addProperty("isPrivate", isPrivate.get())


        json.addProperty("bankAccountNumber", "${bankNumber.get()}")
        json.addProperty("isKFH", isKfh.get())


        loading.set(true)
        disposable.add(
            service.requestBankCallback(json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallbackResponse>() {
                    override fun onSuccess(t: CallbackResponse) {
                        Log.e("requestBankCallback", "onSuccess: $t")
                        loading.set(false)
                        success.value = true
                        t.data?.callback?.let { saveCallback(it) }

                    }

                    override fun onError(e: Throwable) {
                        Log.e("requestBankCallback", "onError: " + e.message)
                        loading.set(false)
                        success.value = false
                        error.value = true
                    }

                })
        )
        //  }
    }

    fun requestBankCallback(bank: Bank) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            return
        }

        if (bankNumber.get()?.length != 14) {
            bankNumberError.value = false
            return
        } else {
            if (CacheObjects.selectedBanks?.indexOf(bank) == CacheObjects.selectedBanks?.size!! - 1) {
                bankNumberError.value = true
            } else {
                bankNumberError.value = false
                return
            }
        }
        loading.set(true)
        val json = JsonObject()
        val phoneJson = JsonObject()
        phoneJson.addProperty("code", "+965")
        phoneJson.addProperty("number", user.phone?.number)
        json.add("phone", phoneJson)
        json.addProperty("fullName", user.fullName)
        json.addProperty("client", "${user.id}")
        if (modelId != "")
            json.addProperty("modelData", modelId)
        json.addProperty("bank", "${bank.id}")
        json.addProperty("loanAmount", loanAmount)
        json.addProperty("totalCost", totalCost)
        json.addProperty("downpayment", downPayment)
        json.addProperty("installmentPeriod", installmentPeriod)
        json.addProperty("installmentAmount", installmentAmount)
        json.addProperty("profit", profit)
        if (netSalary != "")
            json.addProperty("salaryCertificate", netSalary)
        if (aka != "")
            json.addProperty("akamathopia", aka)
        if (bankStatement != "")
            json.addProperty("monthsBankStatement", bankStatement)

        json.addProperty("bankAccountNumber", "${bankNumber.get()}")

        disposable.add(
            service.requestBankCallback(json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallbackResponse>() {
                    override fun onSuccess(t: CallbackResponse) {
                        Log.e("requestBankCallback", "onSuccess: $t")
                        loading.set(false)
                        if (CacheObjects.selectedBanks?.indexOf(bank) == CacheObjects.selectedBanks?.size!! - 1) {
                            success.value = true
                        }
                        t.data?.callback?.let { saveCallback(it) }

                    }

                    override fun onError(e: Throwable) {
                        Log.e("requestBankCallback", "onError: " + e.message)
                        loading.set(false)
                        if (CacheObjects.selectedBanks?.indexOf(bank) == CacheObjects.selectedBanks?.size!! - 1) {
                            error.value = true
                        }

                    }

                })
        )

    }

    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            t.client?.id?.let { it1 ->
                prefs.setString(USER_ID, it1)
            }
            Log.e(TAG, "saveCallback: $it")
        }
    }
}