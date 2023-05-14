package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.Shared.Companion.brands
import com.kuwait.showroomz.extras.Shared.Companion.brandsSimplifierList
import com.kuwait.showroomz.extras.Shared.Companion.modelsList
import com.kuwait.showroomz.extras.Shared.Companion.modelsSimplifierList
import com.kuwait.showroomz.extras.managers.DeviceManger
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.CallbackRequest
import com.kuwait.showroomz.model.simplifier.CallbackWithOutUserRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmObject
import okhttp3.ResponseBody
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.text.isNullOrEmpty

class MainVM : ViewModel() {
    val TAG = "MainVM"
    private val service = ApiService()
    private val local = LocalRepo()
    private val prefs = Shared()
    val ads = MutableLiveData<List<Advertisement>>()
    val settings = MutableLiveData<List<Setting>>()
    val localSettings = MutableLiveData<List<Setting>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    private val disposable = CompositeDisposable()
    var user: User? = null
    var civilId = ObservableField<String>()
    var civilIdError = MutableLiveData<Boolean>(false)
    var adsId = ""
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    val successCallback = MutableLiveData<Boolean>(false)
    var isCivilIdMandatory = ObservableBoolean(false)
    var isKfh = ObservableBoolean(false)
    var isChangeToEmail = ObservableBoolean(false)
    var callbackLoading = MutableLiveData<Boolean>(false)
    var callbackError = MutableLiveData<Boolean>(false)
    val verifyPhone = MutableLiveData<Boolean>(false)
    val emailUpdatedSuccess = MutableLiveData<Boolean>(false)
    var shared = Shared()
    lateinit var modelId: String
    fun showET(): Boolean {
        return isChangeToEmail.get() || isCivilIdMandatory.get()
    }

    init {
        getDeletedObjects()
        //fetchCategories()// done
        //getAds()// done
        //fetchAllAdsActions()// done
        getSettings()// done
        paginLocation(0)
        //getAllTypes()// missing category
        /*if (prefs.existKey(LAST_SESSION)) {
            getDeviceActivityLog(!isSameDay())
            Log.e(TAG, "LAST_SESSION: ${prefs.string(LAST_SESSION)}")
        } else {
            getDeviceActivityLog(true)
        }*/
        if (prefs.existKey(FIRST_INSTALLATION)) {
            Log.e(TAG, "FIRST_INSTALLATION:${prefs.bool(FIRST_INSTALLATION)} ")
            if (!prefs.bool(FIRST_INSTALLATION)) {
                postDeviceRequest()
            }
        } else {
            postDeviceRequest()
        }

        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it
                    fullNameField.set(user?.fullName)
                    phoneNumber.set(user?.phone?.number)
                    civilId.set(user?.civilID)
                }
            }
        }
        modelsList = local.getAll<Model>() as ArrayList<Model>
        modelsSimplifierList = modelsList.toMSimplifier()
        brands = local.getAll<Brand>() as ArrayList<Brand>
        brandsSimplifierList = brands.toBSimplifier()
    }

    fun callUserData() {
        if (prefs.existKey(USER_ID)) {
            prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it
                    fullNameField.set(user?.fullName)
                    phoneNumber.set(user?.phone?.number)
                    civilId.set(user?.civilID)

                }
            }
        }
    }

    fun getAllData() {

        fetchAllBrandsStock()// done
        //fetchAllAgenciesStock()// done
        // if (NetworkUtils.instance.connected) {
        fetchAllActions()// done
        paginModelsStock(0)// done
        fetchAllBank()

        // }
        //val userId = prefs.string(USER_ID)
        // if (userId != ""){
        //getUserData()
        // }
    }

    fun getUserData() {
        getFavorites()
        getUserCars()
    }

    private fun getFavorites() {
        val url = "$FAVORITE_MODELS?_page=0&itemsPerPage=999&customerId=${prefs.string(USER_ID)}"
        disposable.add(
            service.getFavoriteModel(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<java.util.ArrayList<Favorite>>>() {
                    override fun onSuccess(obj: BaseResponse<java.util.ArrayList<Favorite>>) {
                        obj.Result?.let {
                            local.save(it) {}
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }









    private fun getUserCars() {
        val url = "client-vehicles?clientId=${prefs.string(USER_ID)}&_page=0&itemsPerPage=999"
        disposable.add(
            service.getClientVehicleList(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<ClientVehicle>>>() {
                    override fun onSuccess(obj: BaseResponse<ArrayList<ClientVehicle>>) {
                        obj.Result?.let {
                            local.save(it) {}
                        }

                    }

                    override fun onError(e: Throwable) {

                    }
                })
        )
    }
    private fun getDeletedObjects() {
        var url = "DeletedItems"
        if (shared.string("deleted_date") != null && shared.string("deleted_date") != "" ) {
            url += "?deletedAt=${shared.string("deleted_date") }"
        }
        disposable.add(
            service.getDeletedObj(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<Deleted>>() {
                    override fun onSuccess(obj: BaseResponse<Deleted>) {
                        val x = getCurrentDateTime()
                        shared.setString("deleted_date",x.toStringDate())
                        obj.Result?.let {
                            local.delete(Action::class.java,0, it.ActionIds)
                            local.delete(BrandBasic::class.java,0, it.AgencyIds)
                            local.delete(AppraisalInfo::class.java,0, it.AppraisalBrandIds)
                            local.delete(AppraisalInfo::class.java,0, it.AppraisalModelIds)
                            local.delete(CarOption::class.java,0, it.AppraisalVehicleOptionIds)
                            local.delete(Bank::class.java,0, it.BankIds)
                            local.delete(BrandBasic::class.java,0, it.BrandIds)
                            local.delete(Category::class.java,0, it.CategoryIds)
                            local.delete(Industry::class.java,0, it.IndustryIds)
                            local.delete(Type::class.java,0, it.ModelTypeIds)
                            local.delete(Service::class.java,0, it.ServiceIds)
                            local.delete(Location::class.java,0, it.Showroomids)
                            local.delete(Advertisement::class.java,0, it.AdvertisementIds)
                            local.delete(Model::class.java,0, it.ModelDataIds)
                            local.delete(Model::class.java,0, it.ModelIds)
                            local.delete(Trim::class.java,0, it.TrimIds)
                            local.delete(Offer::class.java,0, it.OfferIds)
                        }
                    }

                    override fun onError(e: Throwable) {
                        val x = e.localizedMessage
                        print(e.localizedMessage)
                    }
                })
        )
    }



    private fun saveToLocal(models: List<RealmObject>, url: String) {
        local.save(models) {
            if (it && models.isNotEmpty()) {
                //val x = getCurrentDateTime()
                //prefs.setString("${url}_updatedAt", x.toStringDate())
            }
            Log.e("saved", "$it --- --- $url ----- ${models.size}")

        }
        //getAds()
    }

    private fun fetchAllActions() {
        disposable.add(
            service.getActions(ACTIONS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Action>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Action>>) {
                        obj.Result?.let {
                            saveToLocal(it, ACTIONS)
                        }


                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                    }
                })
        )
    }

    private fun getAllTypes() {
        val url = "model-types"

        disposable.add(
            service.getModelTypeByCategory(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Type>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Type>>) {
                        obj.Result?.let {
                            setTranslationPrimaryKey(it, url)
                        }


                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("Filter", it) }

                    }
                })
        )
    }

    private fun fetchAllAdsActions() {
        disposable.add(
            service.getActions(ACTIONSADS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Action>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Action>>) {
                        obj.Result?.let {
                            saveToLocal(it, ACTIONSADS)
                        }
                        getAllTypes()

                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                        getAllTypes()
                    }
                })
        )
    }

    private fun fetchCategories() {
        disposable.add(
            service.getCategories()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<Category>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Category>>) {
                        obj.Result?.let { list ->
                            // val newCat = list.filter { s -> s.parentObj()?.translations?.en?.name == "Car" && s.translations?.en?.name == "new" }
                            /*if (newCat.isNotEmpty()) {
                                newCat.first().let {
                                    prefs.setString("newCat", it.id)
                                }
                            }*/
                            saveToLocal(list, CATEGORIES_API)
                            getAds()
                        }
                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)
                        getAds()
                    }
                })
        )
    }

    private fun fetchAllBrandsStock() {

        disposable.add(
            service.getBrandsStock(BRANDSTOCKAPI)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<BrandStock>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<BrandStock>>) {
                        obj.Result?.let { list ->
                            saveToLocal(list, BRANDSTOCKAPI)
                        }

                        fetchAllBrands()
                    }

                    override fun onError(e: Throwable) {
                        fetchAllBrands()
                    }
                })
        )
    }

    private fun fetchAllAgenciesStock() {

        disposable.add(
            service.getBrandsStock(AGENCIESSTOCKAPI)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<BrandStock>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<BrandStock>>) {
                        obj.Result?.let { list ->
                            saveToLocal(list, AGENCIESSTOCKAPI)
                            print(list.size)
                        }

                        //fetchAllBrands()
                    }

                    override fun onError(e: Throwable) {
                        print(e.localizedMessage)

                    }
                })
        )
    }

    private fun fetchAllBrands() {
        val executor: Executor = Executors.newSingleThreadExecutor()
        //val url = "$usedFor-datas?_page=$page&itemsPerPage=20"
        disposable.add(
            service.getBrands(BRANDSLIGHT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Brand>>>() {
                    override fun onSuccess(obj: BaseListResponse<List<Brand>>) {
                        obj.Result?.let { list ->
                            saveToLocal(list, BRANDSLIGHT)
                            executor.execute {
                                brands = list as ArrayList<Brand>
                                brandsSimplifierList = brands.toBSimplifier()
                            }

                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e("Error brands", e.localizedMessage)
                        executor.execute {
                            val local2 = LocalRepo()
                            brands = local2.getAll<Brand>() as ArrayList<Brand>
                            brandsSimplifierList = brands.toBSimplifier()
                        }

                        //paginModelsStock(1)
                    }
                })
        )
    }

    private fun paginModelsStock(page: Int) {
        val url = "models?_page=$page&itemsPerPage=400"

        disposable.add(
            service.getModelsStock(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<ModelStock>>>() {
                    override fun onComplete() {

                    }

                    override fun onNext(models: BaseListResponse<List<ModelStock>>) {
                        models.Result?.let {
                            saveToLocal(it, url)
                            if (it.size == 400) {
                                paginModelsStock(page + 1)
                            } else {
                                pagin(0)
                            }
                        }


                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }
                        pagin(0)
                    }
                })
        )
    }


    private fun pagin(page: Int) {
        val url =
            "model-datas-light-get"//String().format(AllModelsApiLight,page)

        val json = JsonObject()
        json.addProperty("_page", page)
        json.addProperty("itemsPerPage", 100)
        val executor: Executor = Executors.newSingleThreadExecutor()
        disposable.add(
            service.getAllBrandModels(url, json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Model>>>() {
                    override fun onComplete() {}

                    override fun onNext(obj: BaseListResponse<List<Model>>) {
                        obj.Result?.let { models ->
                            if (models.isNotEmpty()) {
                                saveToLocal(models, url)
                                pagin(page + 1)
                            } else {
                                executor.execute {
                                    val local2 = LocalRepo()
                                    modelsList = local2.getAll<Model>() as ArrayList<Model>
                                    modelsSimplifierList = modelsList.toMSimplifier()
                                }
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }
                        pagin(page + 1)
                        executor.execute {
                            val local2 = LocalRepo()
                            modelsList = local2.getAll<Model>() as ArrayList<Model>
                            modelsSimplifierList = modelsList.toMSimplifier()
                        }
                    }
                })
        )
    }

    private fun paginLocation(page: Int) {
        val url = "Locations?_page=$page&itemsPerPage=30"

        disposable.add(
            service.getLoctions(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<BaseListResponse<List<Location>>>() {
                    override fun onComplete() {

                    }

                    override fun onNext(models: BaseListResponse<List<Location>>) {
                        models.Result?.let {
                            saveToLocal(it, url)
                            if (it.size == 30) {
                                paginLocation(page + 1)
                            }
                        }


                    }

                    override fun onError(e: Throwable) {
                        e.message?.let { Log.e("MODELS", it) }

                    }
                })
        )
    }

    private fun fetchAllBank() {
        disposable.add(
            service.getBanks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseListResponse<List<Bank>>>() {
                    override fun onSuccess(t: BaseListResponse<List<Bank>>) {
                        t.Result?.let { list ->
                            if (list.isNotEmpty()){
                                saveBanks(list)
                            }
                           
                        }

                    }

                    override fun onError(e: Throwable) {
                        Log.e("fetchAllBank", e.localizedMessage)
                    }

                })
        )
    }

    private fun saveBanks(list: List<Bank>) {
        local.save(list) {
            if (list.isNotEmpty() && it) {
                val x = getCurrentDateTime()
                prefs.setString("bank_updatedAt", x.toStringDate())
            }
            Log.e("saveBanks", it.toString())
        }
    }


    fun getDeviceActivityLogStop() {

        val serialNumber = DeviceManger.getDeviceId()

        disposable.add(
            service.getDeviceActivityLogStop(serialNumber, if (isEnglish) "en" else "ar")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Log.e("ClearFromRecentService", "onSuccess: ${t.string()}")

                    }

                    override fun onError(e: Throwable) {
                        Log.e("ClearFromRecentService", "onError: ${e.localizedMessage}")
                    }

                })
        )
    }

    private fun isSameDay(): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time =
            prefs.string(LAST_SESSION)
                ?.let { Utils.instance.stringToDate(it, "yyyy-MM-dd'T'HH:mm:ss") }
        cal2.time = Date()
        return cal1[Calendar.DAY_OF_YEAR] === cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] === cal2[Calendar.YEAR]
    }

    private fun getDeviceActivityLog(isUnique: Boolean) {

        val serialNumber = DeviceManger.getDeviceId()

        disposable.add(
            service.getDeviceActivityLogStart(
                serialNumber,
                true,
                isUnique,
                if (isEnglish) "en" else "ar"
            )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Utils.instance.dateToString(Date(), "yyyy-MM-dd'T'HH:mm:ss")?.let {
                            prefs.setString(
                                LAST_SESSION,
                                it
                            )
                        }

                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                    }

                })
        )
    }

    private fun postDeviceRequest() {
        val request = JsonObject()
        val serialNumber = DeviceManger.getDeviceId()
        request.addProperty("serialNumber", serialNumber)
        request.addProperty("operatingSystem", "android")
        request.addProperty("language", if (isEnglish) "en" else "ar")
        request.addProperty("deviceId", "")
        disposable.add(
            service.incrementDownload(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(t: ResponseBody) {
                        Log.e(TAG, "onSuccess: ${t.string()}")
                        prefs.setBool(FIRST_INSTALLATION, true)
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ${e.localizedMessage}")
                    }

                })
        )
    }


    fun requestCallback() {
        verifyPhone.value = true
        if (fullNameField.get().isNullOrEmpty()) {
            nameError.value = true
            return
        } else
            nameError.value = false


        if (phoneNumber.get().isNullOrEmpty()) {
            phoneError.value = true
            return
        } else if (phoneNumber.get()?.isValidPhoneNumber() == null) {
            phoneError.value = true
            return
        } else if (phoneNumber.get()?.isValidPhoneNumber() == false) {
            phoneError.value = true
            return
        } else {
            phoneError.value = false
        }


        if (isChangeToEmail.get()) {
            if (!civilId.get().isNullOrEmpty()) {
                if (!Utils.instance.isValidEmail(civilId.get().toString())) {
                    civilIdError.value = true
                    return
                } else
                    civilIdError.value = false
            }
        } else {
            if (isCivilIdMandatory.get()) {
                if (civilId.get().toString().length != 12) {
                    civilIdError.value = true
                    return
                } else
                    civilIdError.value = false

            }
        }

        /*if (!prefs.existKey(USER_ID) && verifyPhone.value == false) {
            verifyPhone.value = true
            return
        }*/



        if (nameError.value == false && phoneError.value == false && civilIdError.value == false) {
            var callback = CallbackWithOutUserRequest(
                fullName = fullNameField.get().toString(),
                phone = Phone("+965", phoneNumber.get().toString())
            )

            if (prefs.existKey(USER_ID)) {
                callback = CallbackRequest(
                    fullName = fullNameField.get().toString(),
                    phone = Phone("+965", phoneNumber.get().toString())
                )
                callback.client = prefs.string(USER_ID)

            }
            if (::modelId.isInitialized) {
                callback.modelData = modelId
            }
            callback.isKFH = isKfh.get()
            if (isChangeToEmail.get()) {
                callback.email = civilId.get()
                if (adsId != "") {
                    callback.advertisement = adsId
                }

                callbackLoading.value = true
                disposable.add(
                    service.sendCallbackAds(callback)
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
                            }

                        })
                )
            } else {
                if (isCivilIdMandatory.get()) {
                    callback.civilId = civilId.get()
                }

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
                json.addProperty("isKFH", isKfh.get())



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
                                //verifyPhone.value = false
//                            nameError.set(true)
//                            phoneError.set(true)
                            }

                        })
                )
            }
        }
    }

    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            Log.e(TAG, "saveCallback: $it")
        }
    }

    private fun getSettings() {


        val listFromLocal = local.getAll<Setting>()
        listFromLocal.let { settings.value = it }

        disposable.add(
            service.getSettings()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<Setting>>() {
                    override fun onSuccess(obj: BaseResponse<Setting>) {
                        obj.Result?.let { t ->

                            if (!listFromLocal.isNullOrEmpty()) {
                                if (t.remoteDataVersion ?: 0 > listFromLocal.first().remoteDataVersion ?: 0) {
                                    updateDB()
                                }
                                t.advertisementModule?.let { prefs.setBool("show_ads", it) }
                            }
                            val list = arrayListOf<Setting>()
                            list.add(t)
                            saveSettings(list)
                            fetchCategories()
                        }


                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        Log.e(TAG, "onError: " + e.localizedMessage)
                        fetchCategories()
                    }

                }
                ))
    }

    private fun updateDB() {
        // var user: User? = null
        var userID: String? = null
        var token: String? = null
        var lastShowAds: String? = null
        if (prefs.existKey(USER_ID)) {
            userID = prefs.string(USER_ID)
            token = prefs.string(TOKEN)
        }
        if (prefs.existKey(LAST_SHOW_TIME_ADS)) {
            lastShowAds = prefs.string(LAST_SHOW_TIME_ADS)
        }
        local.removeDB()
        prefs.removeAll()
        token?.let { getUserInfo(it) }

        if (lastShowAds != null) {
            prefs.setString(LAST_SHOW_TIME_ADS, lastShowAds)
        }
        getAllData()
    }

    private fun getUserInfo(token: String) {
        disposable.add(
            service.getUser(token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<User>() {
                    override fun onSuccess(t: User) {
                        local.saveObject(t) { }
                        prefs.setString(TOKEN, token)
                        t.id?.let { prefs.setString(USER_ID, it) }
                    }

                    override fun onError(e: Throwable) {}
                })
        )
    }

    private fun saveSettings(t: List<Setting>) {
        local.save(t) {
            settings.value = local.getAll<Setting>()
            localSettings.value = local.getAll<Setting>()
        }
    }

    private fun getAds(page: Int = 0) {

        val url = "advertisements?_page=$page&itemsPerPage=50"
        disposable.add(
            service.getAdvertisementsWithPagination(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<BaseListResponse<List<Advertisement>>>() {
                    override fun onSuccess(t: BaseListResponse<List<Advertisement>>) {
                        loading.value = false
                        t.Result?.let {
                            if (it.isNotEmpty()) {
                                saveAds(it)
                                if (it.size == 50)
                                    getAds(page + 1)
                                else
                                    fetchAllAdsActions()
                            }
                        }

                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage
                        loading.value = false
                        fetchAllAdsActions()
                    }
                })
        )
    }

    private fun saveAds(list: List<Advertisement>) {
        local.save(list) {
            ads.value = local.getAll<Advertisement>()
            val x = getCurrentDateTime()
            prefs.setString("ads_updatedAt", x.toStringDate())
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun incrementNbView(ads: Advertisement) {
        disposable.add(
            service.incrementAdsNbView(ads)
                ?.subscribeOn(Schedulers.newThread())
                ?.observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<IncrementNViewResponse>() {
                    override fun onSuccess(t: IncrementNViewResponse) {
                        Log.e(TAG, "onSuccess: " + t.data)
                        ads.id?.let { updateLocalAdvertisement(t, it) }
                    }

                    override fun onError(e: Throwable) {

                    }

                })!!
        )
    }

    fun incrementNbClick(ads: Advertisement) {
        disposable.add(
            service.incrementAdsNbClick(ads)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<IncrementNViewResponse>() {
                    override fun onSuccess(t: IncrementNViewResponse) {
                        //Log.e(TAG, "onSuccess: " + t.data?.nbViews)
                        // ads.id?.let { updateLocalAdvertisement(t, it) }
                    }

                    override fun onError(e: Throwable) {

                    }

                })
        )
    }

    private fun updateLocalAdvertisement(t: IncrementNViewResponse, id: String) {
        local.realm.executeTransaction {
            local.getOne<Advertisement>(id)?.nbViews = t.data ?: 0
        }
    }

    fun getCategory(id: String?): Category? {
        return id?.let { local.getOne<Category>(it) }
    }

    fun getCategoryBySlug(id: String?): Category? {
        return id?.let { local.getOneWithSlug<Category>(it) }
    }

    fun getBrand(id: String?): Brand? {
        return id?.let { local.getOne<Brand>(it) }
    }

    fun getBrandBySlug(id: String?): Brand? {
        return id?.let { local.getOneWithSlug<Brand>(it) }
    }

    fun getModel(id: String?): Model? {
        return id?.let { local.getOne<Model>(it) }
    }

    fun getModelBySlug(id: String?): Model? {
        return id?.let { local.getOneWithSlug<Model>(it) }
    }

    fun getBank(bank: String?): Bank? {
        return bank?.let { local.getOne<Bank>(it) }
    }

    fun updateEmail(email: String, action: (success: Boolean) -> Unit) {
        loading.value = true
        var json = JsonObject()
        json.addProperty("email", email)
        disposable.add(
            service.updateUserInformation(prefs.string(USER_ID)!!, json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BaseResponse<User>>() {
                    override fun onSuccess(obj: BaseResponse<User>) {
                        loading.value = false
                        obj.Result?.let { t ->
                            local.saveObject(t) {}
                            action.invoke(true)
                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        action.invoke(false)
                         //emailUpdatedSuccess.value = false
                    }
                }
                )
        )
    }

    fun setTranslationPrimaryKey(list: List<Type>, url: String) {
        local.save(list) {
            if (it) {
                val x = getCurrentDateTime()
                prefs.setString("${url}_updatedAt", x.toStringDate())
            }
        }
    }
}