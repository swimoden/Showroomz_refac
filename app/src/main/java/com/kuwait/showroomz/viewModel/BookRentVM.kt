package com.kuwait.showroomz.viewModel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
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
import java.util.*
import kotlin.collections.ArrayList

class BookRentVM : ViewModel() {
    val TAG = "BookNowVM"
    private val local = LocalRepo()
    private val prefs = Shared()
    val error = MutableLiveData<Boolean>(false)
    val noConectionError = MutableLiveData<Boolean>(false)
    val loading = ObservableBoolean(false)
    val success = MutableLiveData<Boolean>(false)
    val successUrl = MutableLiveData<String>("")
    val successUploadCivilIdFile = MutableLiveData<Boolean>(false)
    val successUploadLicenseFile = MutableLiveData<Boolean>(false)
    lateinit var model: Model
    private lateinit var postalCode: String
    private val service = ApiService()
    val addons = MutableLiveData<List<Addon>>()
    private val disposable = CompositeDisposable()
    var startDate: Date = Calendar.getInstance().time
    var endDate: Date = Utils.instance.addDaysToDate(Calendar.getInstance().time, 7)!!
    lateinit var startTime: String
    lateinit var endTime: String
    lateinit var trim: Trim
    val paymentMethod = ObservableField<String>("knet")
    lateinit var stringAmount: String
    val addonsFormula = MutableLiveData<ArrayList<AddonsResult>>()
    val amount = ObservableField<String>("")
    val daysAmountFormula = ObservableField<String>("")
    val days = ObservableField<String>("")
    val dailyRate = ObservableField<String>("")
    var uploadLicenseUrl: String = ""
    var listAddons: ArrayList<Addon> = ArrayList<Addon>()
    val fullAddressPickUp = ObservableField<String>("")
    val fullAddressDropOff = ObservableField<String>("")
    var pickUpAddressId: String = ""
    var dropOffAddressId: String = ""
    private var savedLocation: LatLng = LatLng(0.0, 0.0)
    var civilIdUrl: String = ""
    var user: User? = null
    var programs: ArrayList<Program> = ArrayList<Program>()
    val workingHours = MutableLiveData<List<String>>()
    var hash = ""
    var location: Location? = null
    lateinit var offer:OfferSimplifier

    val showDiscount = ObservableBoolean(false)
    val title = ObservableField<String>("")
    val value = ObservableField<String>("")
    init {
        hash = if (BuildConfig.IS_PROD)
            BCrypt.hashpw(BuildConfig.api_Key, BCrypt.gensalt())
        else
            BuildConfig.api_Key

    }

    fun getOffer(modelId: String) {

        local.getOne<TrimResponse>(modelId)?.let{
            val offers = it.offers?.filter { s -> s.isEnabled == true && s.discountValue != null && s.discountValue != "" && s.discountValue != "0"}
            if (offers?.isNotEmpty() == true) {
                offers.first()?.let {
                    offer = OfferSimplifier(it)
                    showDiscount.set(true)
                    title.set(offer.contents?.first())
                    value.set(offer.intDiscount + " %")
                }
            }
        }?: run{
            showDiscount.set(false)
        }



    }

    fun getConnectedUser(): User? {
        noConectionError.value = !NetworkUtils.instance.connected
        user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }
        return user
    }

    fun getAddons() {
        if (!NetworkUtils.instance.connected) {
            return
        }
        val url = "/payment-addons?modelDatasId=${model.id}"
        Log.e(TAG, "getAddons:$url ")
        disposable.add(
            service.getAddons(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<List<Addon>>>() {
                    override fun onSuccess(obj: BaseResponse<List<Addon>>) {
                     obj.Result?.let{
                         addons.value = it.filter { s -> s.isEnabled == true }
                     }

                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onSuccess: ${e.localizedMessage}")
                    }

                }
                ))
    }

    fun postUserAddress(type: Int) {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }
        val json = JsonObject()
        val phoneJson = JsonObject()

        phoneJson.addProperty("code", "+956")
        phoneJson.addProperty("number", user?.phone?.number)
        json.addProperty(
            "name",
            "BOOKING_RENT"
        )
        json.addProperty(
            "type",
            "BOOKING_RENT"
        )
        json.addProperty(
            "address",
            "${fullAddressPickUp.get()}"
        )
        json.add("phone", phoneJson)
        json.addProperty("latitude", savedLocation.longitude)
        json.addProperty("longitude", savedLocation.longitude)
        if (::postalCode.isInitialized)
            json.addProperty(
                "postalCode",
                if (postalCode.isNullOrEmpty()) 0 else postalCode.toInt()
            ) else json.addProperty(
            "postalCode", 0
        )
        json.addProperty("client", "${user?.id}")
        disposable.add(
            service.postUserAddress(json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<UserAddress>>() {
                    override fun onSuccess(obj: BaseResponse<UserAddress>) {
                        obj.Result?.let { t ->
                            if (type == 1) {
                            pickUpAddressId = "${t.id}"
                            dropOffAddressId = "${t.id}"
                        } else dropOffAddressId = "${t.id}" }

                    }

                    override fun onError(e: Throwable) {

                    }

                })
        )

    }

    fun getLocation(location: LatLng) {

        savedLocation = location
        val addresses: List<Address>
        val geocoder = Geocoder(MyApplication.context, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            savedLocation.latitude,
            savedLocation.longitude,
            1
        ) ?: emptyList()

        if (addresses.isNotEmpty()) {
            val address: String = addresses[0].getAddressLine(0) ?: ""
            postalCode = addresses[0].postalCode ?: ""
            if (prefs.existKey("FROM")) {
                if (prefs.int("FROM") == 1) {
                    fullAddressPickUp.set(address)
                    fullAddressDropOff.set(address)
                }
                if (prefs.int("FROM") == 2) {
                    fullAddressDropOff.set(address)
                }
                postUserAddress(prefs.int("FROM"))
            }
            prefs.removeKey("FROM")
            prefs.removeKey("LOCATION")
        }
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
                                uploadLicenseUrl = t.data?.id.toString()
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
    fun updateEmail(email: String, action: (success: Boolean) -> Unit) {
        //loading.value = true
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
                        // emailUpdatedSuccess.value = false
                    }
                }
                )
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
        val total_price = RequestBody.create(
            MultipartBody.FORM,
            Utils.instance.convertToEnglish(stringAmount.replace(",", "."))
        )
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

    fun requestPayment() {
        if (!NetworkUtils.instance.connected) {
            noConectionError.value = true
            loading.set(false)
            return
        }
        val selectedAddonsIds = ArrayList<String>()
        listAddons.forEach {
            selectedAddonsIds.add(it.id)
        }
        val request = user?.id?.let {
            BookNowRentRequest(
                20,
                Utils.instance.dateToString(startDate, "yyyy-MM-dd")!!,
                Utils.instance.convertToEnglish(startTime).toString().replace("p.m", "")
                    .replace("a.m", "")
                    .replace(" صباحا", "").replace(" عصرا", ""),
                Utils.instance.dateToString(endDate, "yyyy-MM-dd")!!,
                Utils.instance.convertToEnglish(endTime).toString().replace("p.m", "")
                    .replace("a.m", "")
                    .replace(" صباحا", "").replace(" عصرا", ""),
                if (pickUpAddressId.isNotEmpty()) pickUpAddressId else null,
                if (dropOffAddressId.isNotEmpty()) dropOffAddressId else null,
                Utils.instance.getDaysBetween(startDate, endDate),
                1,
                stringAmount,
                if (paymentMethod.get() == "knet") 2 else 4,
                2,
                it,
                model.id,
                civilIdUrl,
                uploadLicenseUrl,
                selectedAddonsIds


            )
        }
        loading.set(true)
        disposable.add(
            service.postPaymentBookingRent(request!!)
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
                        error.value = true
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


    fun getHours() {
        val modelSimplifier = ModelSimplifier(model)
        if (modelSimplifier.deliveryHours.size > 0) {
            workingHours.value =
                modelSimplifier.getDeliveryHoursArray(Utils.instance.getDayOfWeekBooking(startDate))
        } else {
            workingHours.value =
                modelSimplifier.getWorkingHoursArray(Utils.instance.getDayOfWeekBooking(startDate))
        }

    }

    fun updateHours() {
        val modelSimplifier = ModelSimplifier(model)
        if (modelSimplifier.deliveryHours.size > 0) {
            workingHours.value =
                modelSimplifier.getDeliveryHoursArray(Utils.instance.getDayOfWeekBooking(startDate))
        } else {
            location?.let {
                workingHours.value = LocationSimplifier(it).getWorkingHoursArray(
                    Utils.instance.getDayOfWeekBooking(startDate)
                )
            }
        }
    }

    fun getModelLocations(brandID: String) {

        val modelSimplifier = ModelSimplifier(model)
        val locs = local.getAllByString<Location>("dealerData.id", brandID)
        if (!locs.isNullOrEmpty()) {
            locs.first { it.type == 2 }.let {
                location = it
                val sim = LocationSimplifier(it)
                fullAddressDropOff.set(sim.name)
                fullAddressPickUp.set(sim.name)
            }


            if (modelSimplifier.deliveryHours.size > 0) {
                workingHours.value = modelSimplifier.getDeliveryHoursArray(
                    Utils.instance.getDayOfWeekBooking(startDate)
                )
            } else {

                location?.let {
                    workingHours.value = LocationSimplifier(it).getWorkingHoursArray(
                        Utils.instance.getDayOfWeekBooking(startDate)
                    )
                }

            }

            loading.set(false)
        } else {
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.set(false)
                return
            }
            val url = "locations?_page=0&itemsPerPage=200&&brandId=$brandID"
            disposable.add(
                service.getLocations(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseListResponse<ArrayList<Location>>>() {
                        override fun onSuccess(obj: BaseListResponse<ArrayList<Location>>) {
                            loading.set(false)
                            obj.Result?.let {list ->
                                if (list.isNotEmpty()) {
                                    setTranslationPrimaryKey(list, brandID)
                                }
                            }

                        }

                        override fun onError(e: Throwable) {


                        }
                    })
            )
        }


    }

    private fun setTranslationPrimaryKey(list: List<Location>, brandID: String) {
        local.save(list) {
            val modelSimplifier = ModelSimplifier(model)
            val location = list.filter { it.type == 2 }.first()
            fullAddressDropOff.set(LocationSimplifier(location).name)
            fullAddressPickUp.set(LocationSimplifier(location).name)
            if (modelSimplifier.deliveryHours.size > 0) {
                workingHours.value =
                    modelSimplifier.getDeliveryHoursArray(
                        Utils.instance.getDayOfWeekBooking(
                            startDate
                        )
                    )
            } else {
                workingHours.value =
                    LocationSimplifier(location).getWorkingHoursArray(
                        4
                    )
//            workingHours.value =
//                LocationSimplifier(location).getWorkingHoursArray(
//                Utils.instance.getDayOfWeek(startDate)
//            )
                Log.e("setTranslationPrimary", "" + it)

            }
        }
    }

    fun calculateAmount() {
        Log.e(TAG, "calculateAmount: ${Utils.instance.getDaysBetween(startDate, endDate)}")
        var dailyProgram: Program? = null
        var monthlyProgram: Program? = null
        var weeklyProgram: Program? = null
        var yearlyProgram: Program? = null
        trim.programs.forEach {
            Log.e(TAG, "calculateAmount: ${it.contractPeriod}")
            if (it.contractPeriod == 1) dailyProgram = it
            if (it.contractPeriod == 7) weeklyProgram = it
            if (it.contractPeriod == 31 || it.contractPeriod == 30) monthlyProgram = it
            if (it.contractPeriod == 365) yearlyProgram = it
        }
        val simp = ModelSimplifier(model)
        val days = Utils.instance.getDaysBetween(startDate, endDate)

        var rate = 0.0
        when {
            days < simp.x1 -> {
                dailyProgram?.monthlyPayment?.let {
                    rate = it.toDouble()
                }
            }
            days <= simp.x2 -> {
                weeklyProgram?.monthlyPayment?.let {
                    rate = it.toDouble() / 7
                }
            }
            days > simp.x2 -> {
                monthlyProgram?.monthlyPayment?.let {
                    rate = it.toDouble() / 30
                }
            }
        }
        dailyRate.set(String.format("%.2f", rate) + if (isEnglish) " KWD " else " د.ك ")
        this.days.set(days.toString())


        var price = rate.times(days)
        addonsFormula.value?.clear()
        daysAmountFormula.set(String.format("%.2f", price) + if (isEnglish) " KWD " else " د.ك ")

        val aux = arrayListOf<AddonsResult>()
        listAddons.forEach {

            if (it.isCollection!!) {
                price = price.plus(days * it.cost.toInt())
                aux.add(
                    AddonsResult(
                        AddonSimplifier(it).name,
                        "$days X ${it.cost} = ${days * it.cost.toInt()} ${if (isEnglish) " KWD " else " د.ك "}"
                    )
                )
            } else {
                price = price.plus(it.cost.toInt())
                aux.add(
                    AddonsResult(
                        AddonSimplifier(it).name,
                        it.cost + " " + if (isEnglish) " KWD " else " د.ك "
                    )
                )
            }
        }
        addonsFormula.value = aux

        if (::offer.isInitialized){
            val x = price * (offer.discountValue / 100)
            price -= x
        }
        //Log.e(TAG, "calculateAmount: $price")
        stringAmount = String.format("%.2f", price)
        amount.set(stringAmount + if (isEnglish) " KWD " else " د.ك ")

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}