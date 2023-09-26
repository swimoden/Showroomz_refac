package com.kuwait.showroomz.viewModel

import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlin.text.isNullOrEmpty

class CarDetailsVM : ViewModel() {
    private val service = ApiService()
    private val disposable = CompositeDisposable()
    val error = MutableLiveData<Boolean>()
    val noConnectionError = MutableLiveData<Boolean> (false)
    val empty = MutableLiveData<Boolean>()
    private val local = LocalRepo()
    private val prefs = Shared()
    val successRequestClientVehicle = MutableLiveData<Boolean>()
    val successRequest = MutableLiveData<Boolean>()
    var brand: AppraisalInfo? = null
    //var model: AppraisalInfo? = null
    var brandName:String? = null
    var modelName =  ObservableField<String>()
    var year: String? = ""
    var cylinders: String? = ""
    var conditions: String? = ""
    val branError = MutableLiveData<Boolean>()
    val imageError = MutableLiveData<Boolean>()
    val modelError = MutableLiveData<Boolean>()
    val yearError = MutableLiveData<Boolean>()
    val engineError = MutableLiveData<Boolean>()
    val cylinderError = MutableLiveData<Boolean>()
    val mileageError = MutableLiveData<Boolean>()
    val conditionError = MutableLiveData<Boolean>()
    val descriptionError = MutableLiveData<Boolean>()
    val engine = ObservableField<String>()
    val mileage = ObservableField<String>()
    val description = ObservableField<String>()
     val imagesArray = arrayListOf<String>()
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var email = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var emailError = MutableLiveData<Boolean>(false)
    var successCallback = MutableLiveData<Boolean>(false)
    val loading = MutableLiveData<Boolean>(false)
    val uploadSuccess = MutableLiveData<Boolean>()
    val options = MutableLiveData<List<CarOption>>()
    val callbacks = MutableLiveData<List<CallbackAppraisalClientVehicle>>()
    val vehicles = MutableLiveData<List<ClientVehicle>>()
    var modelToBuy: Model? = null
    var selectedOptions: ArrayList<CarOption>? = arrayListOf()
    lateinit var clientVehicle: ClientVehicle
    lateinit var user: User

    var paintError = MutableLiveData<Boolean>(false)
    var chasisError = MutableLiveData<Boolean>(false)
    var scratchError = MutableLiveData<Boolean>(false)
    var serviceError = MutableLiveData<Boolean>(false)
    var paint = ObservableField<String>()
    var chasis = ObservableField<String>()
    var scratch:String? = null
    var regularService:String? = null
    var extra = ""
    val list = mutableListOf("", "", "","")
    var uri1:Uri? = null
    var uri2:Uri? = null
    var uri3:Uri? = null
    var uri4:Uri? = null

    init {
        if (prefs.existKey(USER_ID)) {
           prefs.string(USER_ID)?.let {
                local.getOne<User>(it)?.let {
                    user = it
                    fullNameField.set(user.fullName)
                    phoneNumber.set(user.phone?.number)
                    email.set(user.email)
                }
           }
        }
        getOptions()


    }
    fun getUserCars(page:Int = 0){

        val list = prefs.string(USER_ID)?.let {
            local.getAllByString<ClientVehicle>(
                "clientId",
                it
            )
        }
        if (list?.isNotEmpty() == true) {
            list.let{
                vehicles.value = it
            }
        }else{
            getRemoteData(page)
        }


    }
    fun getRemoteData(page:Int){
       // viewModelScope.launch (Dispatchers.IO){
            val url =
                "client-vehicles?clientId=${prefs.string(USER_ID)}&_page=$page&itemsPerPage=2"
            disposable.add(
                service.getClientVehicleList(url)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<BaseResponse<ArrayList<ClientVehicle>>>() {
                        override fun onSuccess(obj: BaseResponse<ArrayList<ClientVehicle>>) {

                            obj.Result?.let { lista ->

                                if (lista.isNotEmpty()) {
                                    local.save(lista) {
                                        val list = prefs.string(USER_ID)?.let {
                                            local.getAllByString<ClientVehicle>(
                                                "clientId",
                                                it
                                            )
                                        }
                                        list?.let {
                                            vehicles.value = it
                                        }

                                        getRemoteData(page + 1)
                                    }
                                }
                            }

                        }

                        override fun onError(e: Throwable) {
                           // getRemoteData(page + 1)
                        }
                    })
            )
       // }
    }

    private fun getVehicles() {
        val callbackList = prefs.string(USER_ID)?.let {
            local.getAllByString<CallbackAppraisalClientVehicle>(
                "client.id",
                it
            )
        }
        callbacks.value = callbackList?.sortedByDescending { it.createdAt }
    }

    private fun saveOptions(t: ArrayList<CarOption>) {
        local.save(t) {

        }
    }

    private fun getOptions() {
        val list = local.getAll<CarOption>()
        if (list?.isNotEmpty() == true) {
            options.value = list.filter { s-> s.isEnabled == true }
        }
        disposable.add(
            service.getOptions()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ArrayList<CarOption>>() {
                    override fun onSuccess(t: ArrayList<CarOption>) {
                        if (t.isNotEmpty()) {
                            options.value = t.filter { s-> s.isEnabled == true }
                            saveOptions(t)
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                })
        )
    }
    fun uploadImageWithIndex(path: String?, from: String, index:Int) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            loading.value = false
            return
        }
        val file = File(path)
        loading.value = true
        val requestFile: RequestBody = RequestBody.create(
            "image/png".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        disposable.add(
            service.uploadImage(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<UploadFileResponse>(){
                    override fun onSuccess(t: UploadFileResponse) {
                        if (t.data?.id != "") {
                            loading.value = false
                            uploadSuccess.value = true
                            //t.data?.id?.let { imagesArray.add(it) }
                            t.data?.id?.let { list.add(index, it) }
                        }else{
                            uploadSuccess.value = false
                        }
                    }
                    override fun onError(e: Throwable) {
                        loading.value = false
                        uploadSuccess.value = false
                        Log.e("CarDetail", "onError: ${e.localizedMessage} ")
                    }
                })

        )
    }
    fun uploadImage(path: String?, from: String) {
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            loading.value = false
            return
        }
        val file = File(path)
        loading.value = true
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
                            loading.value = false
                            uploadSuccess.value = true
                            t.data?.id?.let { imagesArray.add(it) }

                        }else{
                            uploadSuccess.value = false
                        }

                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        uploadSuccess.value = false
                        Log.e("CarDetail", "onError: ${e.localizedMessage} ")
                    }

                })

        )
    }

    fun appraisalRequest() {
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
        if (phoneNumber.get()?.isValidPhoneNumber() == false) {
            phoneError.value = true
            return
        } else
            phoneError.value = false

        if (!Utils.instance.isValidEmail(email.get())) {
            emailError.value = true
            return
        } else emailError.value = false


        if (this::clientVehicle.isInitialized) {
            if (modelToBuy != null) {
                val request =
                    email.get()?.let {
                        fullNameField.get()?.let { it1 ->

                            clientVehicle.id?.let { it2 ->
                                prefs.string(USER_ID)?.let { it3 ->
                                    CallbackAppraisalClientVehicleRequest(
                                        Phone("+965", phoneNumber.get()),
                                        it,
                                        it1,
                                        modelToBuy?.id,
                                        it2,
                                        it3
                                    )
                                }
                            }
                        }
                    }



                loading.value = true
                disposable.add(
                    service.postCallbackAppraisalClientVehicle(request)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :
                            DisposableSingleObserver<CallbackAppraisalClientVehicleResponse>() {
                            override fun onSuccess(t: CallbackAppraisalClientVehicleResponse) {
                                loading.value = false
                                successCallback.value = true
                                saveAppraisalCallback(t.data)
                            }


                            override fun onError(e: Throwable) {
                                loading.value = false
                                error.value = true
                            }

                        })
                )
            }else{
                val request =
                    email.get()?.let {
                        fullNameField.get()?.let { it1 ->

                            clientVehicle.id?.let { it2 ->
                                prefs.string(USER_ID)?.let { it3 ->
                                    CallbackAppraisalClientVehicleRequestWithoutModel(
                                        Phone("+965", phoneNumber.get()),
                                        it,
                                        it1,
                                        it2,
                                        it3
                                    )
                                }
                            }
                        }
                    }



                loading.value = true
                disposable.add(
                    service.postCallbackAppraisalClientVehicleWithoutModel(request)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :
                            DisposableSingleObserver<CallbackAppraisalClientVehicleResponse>() {
                            override fun onSuccess(t: CallbackAppraisalClientVehicleResponse) {
                                loading.value = false
                                successCallback.value = true
                                saveAppraisalCallback(t.data)
                            }


                            override fun onError(e: Throwable) {
                                loading.value = false
                                error.value = true
                            }

                        })
                )
            }
        }

    }

    private fun saveAppraisalCallback(callback: CallbackAppraisalClientVehicle?) {

        if (callback != null) {
            local.saveObject(callback) {
                Log.e("saveAppraisalCallback", "saveAppraisalCallback: +${it}")
            }
        }
    }

    fun requestForPrice() {
        extra = ""
        val new = list.filter { s -> s != "" }
        if (!NetworkUtils.instance.connected) {
            noConnectionError.value = true
            loading.value = false
            return
        }
        if (::clientVehicle.isInitialized) {
            successRequestClientVehicle.value = true
            return
        }
        if (imagesArray.isEmpty() && new.isEmpty()) {
            imageError.value = true
            return
        } else imageError.value = false
        if (brandName.isNullOrEmpty()) {
            branError.value = true
            return
        } else branError.value = false
        if (modelName.get().isNullOrEmpty()) {
            modelError.value = true
            return
        } else modelError.value = false
        if (year.isNullOrEmpty()) {
            yearError.value = true
            return
        } else yearError.value = false
        /*if (engine.get().isNullOrEmpty()) {
            engineError.value = true
            return
        } else engineError.value = false*/

        if (cylinders.isNullOrEmpty()) {
            cylinderError.value = true
            return
        } else cylinderError.value = false
        if (mileage.get().isNullOrEmpty()) {
            mileageError.value = true
            return
        } else mileageError.value = false
       /* if (conditions.isNullOrEmpty()) {
            conditionError.value = true
            return
        } else conditionError.value = false*/
        if (paint.get().isNullOrEmpty()) {
            paintError.value = true
            return
        } else {
            extra += ",pieces painted: ${paint.get()} "
            paintError.value = false
        }

        if (chasis.get().isNullOrEmpty()) {
            chasisError.value = true
            return
        } else {
            extra += ",Chassis condition: ${chasis.get()} "
            chasisError.value = false
        }
        scratch?.let {
            extra += ", $it"
            scratchError.value = false
        } ?: run {
            scratchError.value = true
            return
        }

        regularService?.let {
            extra += ", $it"
            serviceError.value = false
        } ?: run {
            serviceError.value = true
            return
        }

        /*if (description.get().isNullOrEmpty()) {
            descriptionError.value = true
            return
        } else descriptionError.value = false*/


        val request = JsonObject()
        val jsonArrayList = JsonArray()
        imagesArray.forEach {
            jsonArrayList.add(it)
        }
        list.forEach{
            if (it != ""){
                jsonArrayList.add(it)
            }
        }

        request.add("imageGallery", jsonArrayList)
        val optionsJsonArrayList = JsonArray()
        selectedOptions?.forEach {
            optionsJsonArrayList.add(it.id)
        }
        //if (selectedOptions?.isNotEmpty() == true)
            request.add("addOns", optionsJsonArrayList)
        //model?.let {

        // }
        request.addProperty("modelName", modelName.get())
        request.addProperty("dealerName", brandName)
        request.addProperty("clientId", prefs.string(USER_ID))
        request.addProperty("year", year)
        if (!engine.get().isNullOrEmpty()) {
            request.addProperty("engine", engine.get())
        }
        request.addProperty("cylinder", cylinders)
        request.addProperty("mileage", mileage.get())
        if (!conditions.isNullOrEmpty()) {
            request.addProperty("condition", conditions)
        }
        request.addProperty("description", description.get() + extra)

        loading.value = true
        disposable.add(
            service.postClientVehicle(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ClientVehicleResponse>() {
                    override fun onSuccess(response: ClientVehicleResponse) {
                        loading.value = false
                        response.data?.let {
                            successRequestClientVehicle.value = true
                            Log.e("requestForPrice", "onSuccess: ${it.id}")
                            clientVehicle = it
                            saveClientVehicle(it)
                        }?: run{
                            error.value = true
                        }
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        error.value = true
                        Log.e("requestForPrice", "onError: ${e.message}")
                    }

                }
                )
        )

    }

    private fun saveClientVehicle(response: ClientVehicle) {
        local.saveObject(response) {
            Log.e("saveAppraisalCallback", "saveAppraisalCallback: $it")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}