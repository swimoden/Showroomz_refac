package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.HashMap


class BookNowVM : ViewModel() {
    val TAG = "BookNowVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    val loading = ObservableBoolean(false)
    val success = MutableLiveData<Boolean>(false)
    val successUrl = MutableLiveData<String>("")
    val errorUrl = MutableLiveData<String>("")
    val successUploadFile = MutableLiveData<Boolean>(false)
    lateinit var model: Model
    var program: Program? = null
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var civilIdUrl: String = ""
    var user: User? = null
    val paymentMethod = ObservableField<String>("knet")
    var hash = ""
    init {
        hash = if (BuildConfig.IS_PROD)
            BCrypt.hashpw(BuildConfig.api_Key, BCrypt.gensalt())
        else
            BuildConfig.api_Key
    }
    fun getConnectedUser(): User? {
        noConectionError.value = !NetworkUtils.instance.connected
        user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }
        return user
    }

    fun uploadMedia(path: String, from: String) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }
        val file = File(path)

        val requestFile: RequestBody = RequestBody.create(
            "image/png".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        loading.set(true)
        disposable.add(
            service.uploadImage(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<UploadFileResponse>() {
                    override fun onSuccess(t: UploadFileResponse) {
                        if (t.data?.id != "") {
                            successUploadFile.value=true
                            civilIdUrl = t.data?.id.toString()
                        }
                        loading.set(false)

                    }

                    override fun onError(e: Throwable) {
                        Log.e("uploadMedia", "onError: ${e.localizedMessage} ")
                        loading.set(false)
                    }

                })

        )

    }

    fun pay() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }
        val merchant_id = RequestBody.create(MultipartBody.FORM, BuildConfig.merchantid)
        val username = RequestBody.create(MultipartBody.FORM, BuildConfig.username)
        val password = RequestBody.create(MultipartBody.FORM, BuildConfig.password)
        val api_key = RequestBody.create(MultipartBody.FORM, hash)
        val order_id = RequestBody.create(MultipartBody.FORM, model.id)
        val total_price = RequestBody.create(MultipartBody.FORM, model.bookingAmount ?: "0")
        val success_url = RequestBody.create(MultipartBody.FORM, "$BASE_URL/payment/success")
        val error_url = RequestBody.create(MultipartBody.FORM, "$BASE_URL/payment/fail")

        val test_mode = RequestBody.create(MultipartBody.FORM, "${BuildConfig.test_mode}")
        val CstFName = RequestBody.create(MultipartBody.FORM, user?.fullName ?: "")
        val CstEmail = RequestBody.create(MultipartBody.FORM, user?.email ?: "")
        val CstMobile = RequestBody.create(MultipartBody.FORM, UserSimplifier(user ?: User()).phone)
        val ProductName = RequestBody.create(MultipartBody.FORM, ModelSimplifier(model).name)
        val Reference = RequestBody.create(MultipartBody.FORM, "Ref00001")
        val payment_gateway = RequestBody.create(MultipartBody.FORM, paymentMethod.get() ?: "knet")




        val map: MutableMap<String, RequestBody> = HashMap()
        map["merchant_id"] = merchant_id
        map["username"] = username
        map["password"] = password
        map["api_key"] = api_key
        map["order_id"] = order_id
        map["total_price"] = total_price
        map["success_url"] = success_url
        map["error_url"] = error_url
        map["test_mode"] = test_mode
        map["CstFName"] = CstFName
        map["CstEmail"] = CstEmail
        map["CstMobile"] = CstMobile
        map["ProductName"] = ProductName
        map["payment_gateway"] = payment_gateway
        map["Reference"] = Reference
        map["ProductTitle"] = ProductName
        map["ProductPrice"] = total_price
        map["CurrencyCode"] = RequestBody.create(MultipartBody.FORM, "KWD")
        map["ProductQty"] = RequestBody.create(MultipartBody.FORM, "1")

        loading.set(true)
        disposable.addAll(
            service.postPayment(map)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PaymentResponse>() {
                    override fun onSuccess(t: PaymentResponse) {
                        Log.e(TAG, "onSuccess: ${t.status}")
                            if (t.status=="success")
                            successUrl.value=t.paymentURL
                            if (t.status =="errors")
                                error.value =true

                        loading.set(false)

                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                        loading.set(false)
                        error.value =true
                    }

                })
        )

    }

    fun requestPayment() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }
        val bookNowRequest = model.bookingAmount?.let {
            user?.id?.let { it1 ->
                BookNowRequest(
                    10,
                    it,
                    if (paymentMethod.get() =="knet")2 else 4,
                    2,
                    it1,
                    if (program == null) null else program?.id,
                    model.id,
                    civilIdUrl
                )
            }
        }
        loading.set(true)
        disposable.add(
            service.postPaymentBooking(bookNowRequest!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<Booking>>() {
                    override fun onSuccess(t: BaseResponse<Booking>) {
                       loading.set(false)
                        success.value = true
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                        loading.set(false)
                        error.value =true
                    }

                })
        )

    }
    fun updateEmail(email: String, action: (success: Boolean) -> Unit) {
        val json = JsonObject()
        json.addProperty("email", email)
        disposable.add(
            service.updateUserInformation(prefs.string(USER_ID)!!, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                    override fun onSuccess(obj: BaseResponse<User>) {
                        obj.Result?.let { t ->
                            local.saveObject(t) {}
                            action.invoke(true)
                        }
                    }

                    override fun onError(e: Throwable) {
                        action.invoke(false)
                    }
                })
        )
    }

    private fun saveBooking(t: Booking) {
        local.saveObject(t) {
            if (it)
                success.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}