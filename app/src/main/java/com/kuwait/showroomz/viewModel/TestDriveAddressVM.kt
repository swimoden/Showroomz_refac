package com.kuwait.showroomz.viewModel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TestDriveParamsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.text.isNullOrEmpty


class TestDriveAddressVM : ViewModel() {

    var selectedAddress: UserAddress? = null
    private lateinit var postalCode: String
    private var savedLocation: LatLng = LatLng(0.0, 0.0)
    val areaInput = ObservableField<String>("")
    val street = ObservableField<String>("")
    val block = ObservableField<String>("")
    val avenue = ObservableField<String>("")
    val building = ObservableField<String>("")
    val phone = ObservableField<String>("")
    val fullAddress = ObservableField<String>("")
    val areaError = ObservableBoolean(false)
    val streetError = ObservableBoolean(false)
    val blockError = ObservableBoolean(false)
    val avenueError = ObservableBoolean(false)
    val buildingError = ObservableBoolean(false)
    val phoneError = ObservableBoolean(false)
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    private val local = LocalRepo()
    private val prefs = Shared()
    val loading = MutableLiveData<Boolean>()
    val loadingRequest = MutableLiveData<Boolean>(false)
    val submitLoading = ObservableBoolean(false)
    val addresses = MutableLiveData<List<UserAddress>>()
    val empty = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var email = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneBShError = MutableLiveData<Boolean>(false)
    var emailError = MutableLiveData<Boolean>(false)
    var addressType = ObservableField<String>()
    var type = ObservableField<String>()
    var addressName = ObservableField<String>()
    private val gson = GsonBuilder().create()
    lateinit var user: User
    lateinit var model: Model
    lateinit var modelSimplifier: ModelSimplifier
    val successRequestDrive = MutableLiveData<Boolean>()
    val noConectionError = MutableLiveData<Boolean>( false)
    init {
        if (prefs.existKey(USER_ID)) {
            user = prefs.string(USER_ID)?.let { local.getOne<User>(it) }!!
            phoneNumber.set(user.phone?.number)
            email.set(user.email)
            fullNameField.set(user.fullName)
            phone.set(user.phone?.number)
        }


        if (prefs.existKey(MODEL_ID)) {
           modelSimplifier=ModelSimplifier( prefs.string(MODEL_ID)?.let { local.getOne<Model>(it) }!!)
        }
    }

     fun getModelData():Model {
        return prefs.string(MODEL_ID)?.let { local.getOne<Model>(it) }!!
    }

    fun getLocation(location: LatLng) {

        savedLocation = location
        val addresses: List<Address>
        val geocoder: Geocoder = Geocoder(MyApplication.context, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )?: emptyList()


        val address: String = addresses[0].getAddressLine(0) ?: ""
        /*var area = addresses[0].adminArea ?: ""
        val city: String = addresses[0].locality ?: ""
        val state: String = addresses[0].adminArea ?: ""*/
        postalCode = addresses[0].postalCode ?: ""
        //val streetName: String = addresses[0].thoroughfare ?: ""
        areaInput.set("")
        street.set("")
        phone.set(user.phone?.number)
        block.set("")
        avenue.set("")
        building.set("")
        fullAddress.set(address)

    }

    fun getUserAddress() {

        loading.value = true
        val userAddresses = local.getAll<UserAddress>()
        if (userAddresses?.isNotEmpty()!!) {
            addresses.value = userAddresses.filter { it.clientId == user.id }
            loading.value = false
        }else{
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.value = false
                addresses.value = emptyList()
                return
            }
        }
        val url = "$CLIENT_ADDRESS?clientId=${user.id}"
        disposable.add(
            service.getUserAddresses(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<UserAddress>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<UserAddress>>) {
                        loading.value = false
                        obj.Result?.let { t ->
                            if (t.isNotEmpty())
                                saveAddresses(t)
                        }

                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                    }

                })
        )
    }

    private fun saveAddresses(t: java.util.ArrayList<UserAddress>) {
        local.save(t) {
            val userAddresses = local.getAll<UserAddress>()
            addresses.value = userAddresses?.filter { it.clientId == user.id }
        }
    }

    fun validFields(): Boolean {
        if (!fullAddress.get().isNullOrEmpty()) {
            areaError.set(false)
            streetError.set(false)
            blockError.set(false)
            buildingError.set(false)
            phoneError.set(false)
            return true
        } else {
            if (areaInput.get().isNullOrEmpty()) areaError.set(true) else areaError.set(false)
            if (street.get().isNullOrEmpty()) streetError.set(true) else streetError.set(false)
            if (block.get().isNullOrEmpty()) blockError.set(true) else blockError.set(false)
            if (building.get()
                    .isNullOrEmpty()
            ) buildingError.set(true) else buildingError.set(false)
            if (phone.get().isNullOrEmpty()) phoneError.set(true) else phoneError.set(false)
            if (
                !areaError.get() &&
                !streetError.get() &&
                !blockError.get() &&
                !buildingError.get() &&
                !phoneError.get()
            )
                return true
        }
        return false
    }

    fun postUserAddress() {

        if (!NetworkUtils.instance.connected){
            noConectionError.value = true
            loading.value = false
        }else {
            if (fullNameField.get().isNullOrEmpty()) {
                nameError.value = true
                return
            } else
                nameError.value = false
            //if (phoneNumber.get().isNullOrEmpty() || phoneNumber.get()?.length != 8) {
            phoneBShError.value = !phoneNumber.get().isValidPhoneNumber()
           /* if (phoneNumber.get()?.isValidPhoneNumber() == true) {
                phoneBShError.value = true
                return
            } else
                phoneBShError.value = false*/

            if (!Utils.instance.isValidEmail(email.get())) {
                emailError.value = true
                return
            } else emailError.value = false
            if (selectedAddress != null) {
                submitLoading.set(true)
                requestTestDriveToMyLocation(selectedAddress!!)
                return
            }
            val json = JsonObject()
            if (fullAddress.get().isNullOrEmpty()) {
                json.addProperty(
                    "address",
                    "${areaInput.get()},${street.get()},${block.get()},${avenue.get()},${building.get()}"
                )
            } else json.addProperty(
                "address",
                "${fullAddress.get()}"
            )
            json.addProperty(
                "name",
                "${addressType.get()?.toLowerCase()}"
            )
            json.addProperty(
                "type",
                "${type.get()}"
            )
            val phoneJson = JsonObject()

            phoneJson.addProperty("code", "+956")
            phoneJson.addProperty("number", phone.get())


            if (nameError.value == false && phoneBShError.value == false && emailError.value == false) {


                json.addProperty("email", user.email)
                json.addProperty("latitude", savedLocation.longitude)
                json.addProperty("longitude", savedLocation.longitude)
                if (::postalCode.isInitialized)
                    json.addProperty(
                        "postalCode",
                        if (postalCode.isNullOrEmpty()) 0 else postalCode.toInt()
                    ) else json.addProperty(
                    "postalCode", 0
                )
                json.addProperty("clientId", user.id)
                json.add("phone", phoneJson)
                submitLoading.set(true)
                disposable.add(
                    service.postUserAddress(json)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :
                            DisposableSingleObserver<BaseResponse<UserAddress>>() {
                            override fun onSuccess(obj: BaseResponse<UserAddress>) {
                                obj.Result?.let { t ->
                                    saveAddress(t)
                                    requestTestDriveToMyLocation(t)
                                }
                            }
                            override fun onError(e: Throwable) {
                                submitLoading.set(false)
                                error.value = e.localizedMessage
                            }
                        }
                        )
                )
            }
        }
    }

    private fun requestTestDriveToMyLocation(t: UserAddress) {
        if (!NetworkUtils.instance.connected){
            noConectionError.value = true
            loadingRequest.value = false
            submitLoading.set(false)
        }else {




            val json = JsonObject()
            val phoneJson = JsonObject()

            phoneJson.addProperty("code", "+956")
            phoneJson.addProperty("number", phoneNumber.get())
            json.addProperty("client", user.id)
            json.addProperty("address", t.id)
            json.addProperty("email", user.email)
            json.addProperty("fullName", user.fullName)
            json.add("phone", phoneJson)
            json.addProperty("modelData", prefs.string(MODEL_ID))
            json.addProperty("preferredTime", prefs.string(PREFERRED_TIME) + ":00.000Z")
            json.addProperty("location","1")
            loadingRequest.value = true
            disposable.add(
                service.requestTestDriveWithDelivery(json)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<TestDriveParamsResponse>() {
                        override fun onSuccess(t: TestDriveParamsResponse) {
                            submitLoading.set(false)
                            loadingRequest.value = false
                            t.data?.testDrive?.let { saveTestDrive(it) }
                            t.data?.testDrive?.client?.id?.let { prefs.setString(USER_ID, it) }
                            t.data?.token?.let { prefs.setString(TOKEN, it) }
                            successRequestDrive.value = true
                        }

                        override fun onError(e: Throwable) {
                            submitLoading.set(false)
                            loadingRequest.value = false
                            error.value = e.message
                        }

                    })
            )
        }
    }
    private fun saveUser(it: User) {
        local.saveObject(it) {
            Log.e("saveUser", "saveUser: $it ")
        }
    }
    private fun saveAddress(t: UserAddress) {
        local.saveObject(t) {
            Log.e("saveAddress", "saveAddress: $t")
        }
    }

    private fun saveTestDrive(t: TestDrive) {
        local.saveObject(t) {

        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun putAddress(address: UserAddress?) {
        try {
            fullAddress.set(address?.address)
//            getLocation(location)
            /*  areaInput.set(address?.address?.let { stringToWords(it)[0] })
              street.set(address?.address?.let { stringToWords(it)[1] })
              block.set(address?.address?.let { stringToWords(it)[2] })
              avenue.set(address?.address?.let { stringToWords(it)[3] })
              building.set(address?.address?.let { stringToWords(it)[4] })*/

        } catch (e: IndexOutOfBoundsException) {
        }

    }

    fun stringToWords(s: String) = s.trim().splitToSequence(',')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    fun isNullAddress(): Boolean {
        return savedLocation.latitude==0.0||savedLocation.longitude==0.0
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
}