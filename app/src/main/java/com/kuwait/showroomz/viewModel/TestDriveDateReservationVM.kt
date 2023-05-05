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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class TestDriveDateReservationVM : ViewModel() {
    var toMylocation: Boolean = false
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    var date = ObservableField<Date>()
    private val prefs = Shared()
    var selectedDate = ObservableField<Date>()
    var time = ObservableField<String>("")
    private val local = LocalRepo()
    val locations = MutableLiveData<List<Location>>()
    val loading =  ObservableBoolean(false)
    val requestTestDriveLoading =  MutableLiveData<Boolean>(false)
    val errorLocation = MutableLiveData<Boolean>(false)
    val empty = MutableLiveData<Boolean>()
    val successRequestDrive = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val preferredTimeList = MutableLiveData<ArrayList<PreferredTimeTestDrive>>()
    lateinit var user: User
    lateinit var model: Model
    var selectedLocationID: String? = ""
    var selectedLocation: Location? = null
    var testDriveStatus: Int = -1
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var email = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var emailError = MutableLiveData<Boolean>(false)
    fun isInitializedLocation() = selectedLocation != null
    val noConectionError = MutableLiveData<Boolean>( false)
    val verifyPhone = MutableLiveData<Boolean>(false)

    init {
        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let { user ->
                    this.user = user
                    phoneNumber.set(user.phone?.number)
                    email.set(user.email)
                    fullNameField.set(user.fullName)
                }
            }

        }
    }

    fun getModelLocations(brandID: String) {

        loading.set(true)
        val locs = local.getAllByInt<Location>("brandId", brandID.toInt())
        if (!locs.isNullOrEmpty()) {
            locations.value = locs.filter { it.type == 10 && it.isEnabled == true }
            loading.set(false)
        }else{
            if (!NetworkUtils.instance.connected) {
                noConectionError.value = true
                loading.set(false)
                empty.value = true
                return
            }
        }

        val url = "locations?_page=0&itemsPerPage=200&&brandId=$brandID"
        disposable.add(
            service.getLocations(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<ArrayList<Location>>>() {
                    override fun onSuccess(obj: BaseListResponse<ArrayList<Location>>) {
                        loading.set(false)

                        ///setTranslationPrimaryKey(obj.Result!!, brandID)
                        obj.Result?.let { list ->
                            if (list.isNotEmpty()) {
                                setTranslationPrimaryKey(list, brandID)
                            }else if (locs.isNullOrEmpty())
                                empty.value = true
                        }?: kotlin.run {
                            empty.value = true
                        }
                    }
                    override fun onError(e: Throwable) {
                        loading.set(false)
                        empty.value = locs?.isEmpty()
                    }
                })
        )
    }

    private fun setTranslationPrimaryKey(list: List<Location>, brandID: String) {
        local.save(list) {
            loading.set(false)

            locations.value = list.filter { it.type == 10  && it.isEnabled == true}
            Log.e("setTranslationPrimary", "" + it)

        }
    }

    fun requestTestDriveAtShowroom() {
        if (!NetworkUtils.instance.connected) {
            loading.set(false)
            noConectionError.value = true
        }else {
            if (fullNameField.get().isNullOrEmpty()) {
                nameError.value = true
                return
            } else
                nameError.value = false

            if (phoneNumber.get()?.isValidPhoneNumber() == null) {
                phoneError.value = true
                return
            } else if ( !phoneNumber.get()!!.isValidPhoneNumber()){
                phoneError.value = true
                return
            }else{
                phoneError.value = false
            }

            if (!Utils.instance.isValidEmail(email.get())) {
                emailError.value = true
                return
            } else emailError.value = false

           /* if (!isConnected() && verifyPhone.value == false) {
                verifyPhone.value = true
                return
            }*/

            if (nameError.value == false && phoneError.value == false && emailError.value == false) {
                val x = time.get().toString()
                var timing = Utils.instance.convertToEnglish(time.get().toString()).toString().replace("p.m", "").replace("a.m", "")
                    .replace(" صباحا","").replace(" عصرا","")
                if (x.contains("p.m") || x.contains(" عصرا")){
                    val t = Utils.instance.convertToEnglish(time.get().toString()).toString().replace("p.m", "").replace(" عصرا","").trim()
                    if (t.contains(":")){
                        val container = t.split(":")
                        val x1 = container[0].toInt()
                        val x2 = container[1]
                        val f = x1+12
                        timing = "$f:$x2"
                    }
                }

                val preferredTime =
                    selectedDate.get()?.let {
                        Utils.instance.dateToString(
                            it,
                            "yyyy-MM-dd"
                        )
                    } + "T$timing"
                val json = JsonObject()
                val phoneJson = JsonObject()
                phoneJson.addProperty("code", "+965")
                phoneJson.addProperty("number", phoneNumber.get())
                if (isConnected()) json.addProperty("client", user.id)

                json.addProperty("location", selectedLocationID)
                json.addProperty("email", email.get())
                json.addProperty("fullName", fullNameField.get())
                json.add("phone", phoneJson)
                json.addProperty("modelData", model.id)
                json.addProperty("preferredTime", preferredTime)
                requestTestDriveLoading.value = true
                disposable.add(
                    service.requestTestDriveAtShowrooms(json)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :
                            DisposableSingleObserver<BaseResponse<TestDrive>>() {
                            override fun onSuccess(obj: BaseResponse<TestDrive>) {
                                requestTestDriveLoading.value = false
                                obj.Result?.let { t ->
                                    if (!prefs.existKey(USER_ID)) {
                                        saveTestDrive(t)
                                        t.client?.let { user ->
                                            this@TestDriveDateReservationVM.user = user
                                            user.id?.let {
                                                prefs.setString(USER_ID, it)
                                                prefs.setString(TOKEN, it)
                                            }
                                        }
                                        t.client?.id?.let {
                                            prefs.setString(USER_ID, it)
                                        }
                                    }
                                    successRequestDrive.value = true
                                }?: kotlin.run {
                                    successRequestDrive.value = false
                                }

                            }

                            override fun onError(e: Throwable) {
                                requestTestDriveLoading.value = false
                                error.value = e.message
                                verifyPhone.value = false
                            }
                        })
                )
            }
        }
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
    private fun saveUser(it: User) {
        local.saveObject(it) {
            Log.e("saveUser", "saveUser: $it ")
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

    fun getPreferredTimeList(date: Date?) {
        val after =  date?.let {
            Utils.instance.addDaysToDate(it, -1)
                ?.let { Utils.instance.dateToString(it, "yyyy-MM-dd") }
        }
        val before =
            date?.let {
                Utils.instance.addDaysToDate(it, 1)
                    ?.let { Utils.instance.dateToString(it, "yyyy-MM-dd") }
            }
        val url = if (isInitializedLocation()) {
            "$GET_PREFERRED_TIME?locationId=${selectedLocationID}&modelDataId=${model.id}&preferredTimeAfter=${after}&preferredTimeBefore=$before"
        } else {
            "$GET_PREFERRED_TIME?modelDataId=${model.id}&preferredTimeAfter=${after}&preferredTimeBefore=$before"
        }

        disposable.add(
            service.getPreferredTime(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseResponse<ArrayList<PreferredTimeTestDrive>>>() {
                    override fun onSuccess(t: BaseResponse<ArrayList<PreferredTimeTestDrive>>) {

//                            savePreferredTimeByLocation(t)
                        t.Result?.let {
                            preferredTimeList.value = it
                        }

                    }

                    override fun onError(e: Throwable) {
                        Log.e("getPreferredTimeList", "onError: ${e.localizedMessage}")
                    }

                })
        )
    }

    fun savePreferredTime() {
        val x = time.get().toString()
        var timing = Utils.instance.convertToEnglish(time.get().toString()).toString().replace("p.m", "").replace("a.m", "")
            .replace(" صباحا","").replace(" عصرا","")
        if (x.contains("p.m") || x.contains(" عصرا")){
            val t = Utils.instance.convertToEnglish(time.get().toString()).toString().replace("p.m", "").replace(" عصرا","").trim()
            if (t.contains(":")){
                val container = t.split(":")
                val x1 = container[0].toInt()
                val x2 = container[1]
                val f = x1+12
                timing = if (f < 10) {
                    "0$f:$x2"
                }else{
                    "$f:$x2"
                }
            }
        }
        val preferredTime = selectedDate.get()?.let {
            Utils.instance.dateToString( it,"yyyy-MM-dd")
        } + "T$timing"

        prefs.setString(PREFERRED_TIME, preferredTime)
        prefs.setString(MODEL_ID, model.id)
    }

    fun isConnected() = prefs.existKey(USER_ID)
}