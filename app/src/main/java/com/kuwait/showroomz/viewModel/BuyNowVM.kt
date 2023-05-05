package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.context
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

class BuyNowVM : ViewModel() {
    val TAG = "BuyNowVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    val loading = ObservableBoolean(false)
    val success = MutableLiveData<Boolean>(false)
    lateinit var model: Model
    lateinit var trim: Trim
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var civilIdUrl: String = ""
    var uploadLicenseUrl: String = ""
    var user: User? = null
    var offer: Offer? = null
    var offers :ArrayList<OfferSimplifier> = arrayListOf()
    var selectedOffers :ArrayList<OfferSimplifier> = arrayListOf()
    var details:MutableLiveData<ArrayList<OfferDetail>> = MutableLiveData()
    var selectedOffersObj :ArrayList<Offer> = arrayListOf()

    lateinit var amountPaid: String
    lateinit var offerSimplifier: OfferSimplifier
    val successUploadCivilIdFile = MutableLiveData<Boolean>(false)
    val successUploadLicenseFile = MutableLiveData<Boolean>(false)
    val totalString = ObservableField<String>("")
    val successUrl = MutableLiveData<String>("")
    val errorUrl = MutableLiveData<String>("")
    val paymentMethod = ObservableField<String>("knet")
    var hash = ""
    var showChangeOffer = false
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

    fun uploadMedia(path: String, from: String, code: Int) {
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
                            if (code == 1) {
                                civilIdUrl = t.data?.id.toString()
                                successUploadCivilIdFile.value = true
                            }
                            if (code == 2) {
                                uploadLicenseUrl =
                                    t.data?.id.toString()
                                successUploadLicenseFile.value = true
                            }
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


    private fun saveBooking(t: Booking) {
        local.saveObject(t) {
            if (it)
                success.value = true
        }
    }

    fun getExclusiveOffers(): Offer? {
        val trimResponse = local.getOne<TrimResponse>(model.id)
        if (trimResponse != null) {
            val exclusiveOffer = trimResponse.offers?.filter { it -> it.type == 2 }
            if (exclusiveOffer != null && exclusiveOffer.isNotEmpty()) {
                trimResponse.offers?.get(0)?.let {
                    offerSimplifier = OfferSimplifier(it)
                }
                return exclusiveOffer.first()
            }

        }
        return null
    }
fun showSelectOffer(id:String):Boolean{

    return local.getOne<TrimResponse>(id)?.let{
        it.offers?.size?.let {
            it > 0
        }
    } == false
}
    fun getOffers(id:String){
        offers.clear()
        local.getOne<TrimResponse>(id)?.let {
            it.offers?.forEach {
                if (it.isEnabled == true)
                offers.add(OfferSimplifier(it))
            }
        }
        selectedOffers = offers
    }

    fun calculateList(){
        val priceformated = if (trim.price.toInt() == 0) model.price().toInt() else trim.price


        var totalDiscount = 0
        val list:ArrayList<OfferDetail> = arrayListOf()
        selectedOffers.forEach {
            val amount = it.intDiscount.replace(",","").replace(".","").toInt()
            totalDiscount += amount
            if (it.type == 8){
                if (amount > 0){
                    list.add(OfferDetail((it.name ?: context.getString(R.string.discount)),it.discount,0))
                }else{
                    if (list.isEmpty()){
                        list.add(OfferDetail((context.getString(R.string.gifts_provided)),"",2))
                    }
                }
                it.contents?.forEach {
                    list.add(OfferDetail(it, "",1))
                }
            }else{
                list.add(OfferDetail((it.name ?: context.getString(R.string.discount)),it.discount, 0))
                if (selectedOffers.size > 1 && it.id == selectedOffers.first().id){
                    list.add(OfferDetail((context.getString(R.string.providing_gift)),"",2))
                }
                if (it.type == 2){
                    it.contents?.forEach {
                        list.add(OfferDetail(it, "",1))
                    }
                }
            }
        }
        val total = priceformated - totalDiscount

        details.value = list
        amountPaid = total.toString()
        totalString.set(priceFormatter(total.toString()))
    }


    fun calculate() {
        val priceformated = if (trim.price.toInt() == 0) model.price().toInt() else trim.price

        var total: Int = 0
        if (offer != null) {
            if (offer?.type != 2) {
                val exclusiveOffer = getExclusiveOffers()
                val ex_discount = exclusiveOffer?.discountValue
                val of_discount =  offer?.discountValue
                var totalDiscount = 0
                ex_discount?.let{
                    totalDiscount += it.toInt()
                }
                of_discount?.let{
                    totalDiscount += it.toInt()
                }
                total = priceformated - totalDiscount
            } else {
                total = offer?.discountValue?.replace(",","")?.replace(".","")?.toInt()?.let { priceformated.minus(it) }!!
            }
        } else {
            total = priceformated
        }
        amountPaid = total.toString()
        totalString.set(priceFormatter(total.toString()))
    }

     fun priceFormatter(pPrice: String): String {
        return if (isEnglish) "${pPrice.delimiter()} KWD " else "${pPrice.delimiter()} د.ك "
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
        val total_price = RequestBody.create(MultipartBody.FORM, amountPaid)
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
                        if (t.status == "success")
                            successUrl.value = t.paymentURL
                        if (t.status == "errors")
                            error.value = true



                        loading.set(false)

                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                        loading.set(false)
                        error.value = true
                    }

                })
        )

    }

    fun buyNow() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }

        val array = arrayListOf<String>()
        if (offer != null) {
            array.add("${offer?.id}")
            if (offer?.type != 2) {
                var exculusie = getExclusiveOffers()
                if (exculusie != null) {
                    array.add(exculusie.id)
                }
            }
        }


        val request = user?.id?.let {
            BuyNowRequest(
                30,
                trim.price.toString(),
                amountPaid,
                if (paymentMethod.get() == "knet") 2 else 4,
                2,
                it,
                model.id,
                civilIdUrl,
                uploadLicenseUrl,
                array
            )
        }
        loading.set(true)
        disposable.add(
            service.postPaymentBuying(request!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<Booking>>() {
                    override fun onSuccess(t: BaseResponse<Booking>) {
                        //saveBooking(t)
                        loading.set(false)
                        success.value = true
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                        loading.set(false)
                        error.value = true
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
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}