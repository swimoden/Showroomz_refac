package com.kuwait.showroomz.viewModel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.api.ApiService
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.LocationCallbackRequest
import com.kuwait.showroomz.model.simplifier.LocationCallbackWithOutUserRequest
import com.kuwait.showroomz.model.simplifier.LocationSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class LocationVM : ViewModel() {
    private val TAG = "LocationVM"
    private val service = ApiService()
    lateinit var location: LocationSimplifier
    private val disposable = CompositeDisposable()
    val locations = MutableLiveData<List<Location>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val empty = MutableLiveData<Boolean>()
    val successCallback = MutableLiveData<Boolean>(false)
    var isCivilIdMandatory = ObservableBoolean(false)
    var fullNameField = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var email = ObservableField<String>()
    var nameError = MutableLiveData<Boolean>(false)
    var phoneError = MutableLiveData<Boolean>(false)
    var civilIdError = MutableLiveData<Boolean>(false)
    var civilId = ObservableField<String>()

    var emailError = ObservableBoolean(false)
    var callbackLoading = MutableLiveData<Boolean>(false)
    var callbackError = MutableLiveData<Boolean>(false)
    val prefs = Shared()
    private val local = LocalRepo()
    private lateinit var user: User

    val noConnectionError = MutableLiveData<Boolean>(false)
    val verifyPhone = MutableLiveData<Boolean>(false)
    var newList: ArrayList<Location> = arrayListOf()

    fun getUser() {
        if (prefs.existKey(USER_ID)) {

            prefs.string(USER_ID)?.let {
                 local.getOne<User>(it)?.let { u ->
                     user = u
                     fullNameField.set(user.fullName)
                     phoneNumber.set(user.phone?.number)
                     email.set(user.email)
                     civilId.set(user.civilID)
                 }
            }
        }

    }

    fun refresh(ids: List<String> = arrayListOf()) {
        fetchBrands(ids)
    }

    private fun fetchBrands(ids: List<String> = arrayListOf()) {
        //newList.clear()
        newList = ArrayList()
        loading.value = true
        val localList: ArrayList<Location> = arrayListOf()
        for (item in ids) {
            val locs = local.getAllByInt<Location>("brandId", item.toInt())
            locs?.let {
                localList.addAll(it)
            }
        }

        if (localList.size > 0) {
            loading.value = false
            error.value = ""
            locations.value = localList.sortedBy { it.type }
        }else{
            if (!NetworkUtils.instance.connected) {
                noConnectionError.value = true
                loading.value = false
                empty.value = true
                return
            }
        }
        fetchRemote(ids, 0)


    }
    fun fetchRemote(ids:List<String>, position:Int){
       // var url = "locations?_page=0&itemsPerPage=200"
       // for (item in ids) {
       if (position < ids.size) {
           val url = "locations?_page=0&itemsPerPage=200&brandId=${ids[position]}"
           //}
           disposable.add(
               service.getLocations(url)
                   .subscribeOn(Schedulers.newThread())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribeWith(object :
                       DisposableSingleObserver<BaseListResponse<ArrayList<Location>>>() {
                       override fun onSuccess(obj: BaseListResponse<ArrayList<Location>>) {
                           obj.Result?.let { list ->
                               if (list.isNotEmpty()) {
                                   newList.addAll(list)
                                   setTranslationPrimaryKey(list)
                                   fetchRemote(ids, position + 1)
                                   locations.value = newList.sortedBy { it.type }
                               }
                           }
                       }

                       override fun onError(e: Throwable) {
                           error.value = e.localizedMessage
                           loading.value = false
                           //empty.value = localList.isEmpty()
                       }
                   })
           )
       }else{
           //locations.value = newList.sortedBy { it.type }
       }
    }

    fun setTranslationPrimaryKey(list: List<Location>) {
        local.save(list) {
            loading.value = false
            error.value = ""
            locations.value = list.sortedBy { it.type }
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

        if (phoneNumber.get()?.isValidPhoneNumber() == true) {
            phoneError.value = false
        } else {
            phoneError.value = true
            return
        }

        if (isCivilIdMandatory.get()) {
            if (civilId.get().toString().length != 12) {
                civilIdError.value = true
                return
            } else
                civilIdError.value = false

        }

        /*if (!prefs.existKey(USER_ID) && verifyPhone.value == false) {
            verifyPhone.value = true
            return
        }*/
        if (nameError.value == false && phoneError.value == false && civilIdError.value == false) {
            var callback = LocationCallbackWithOutUserRequest(
                fullName = fullNameField.get().toString(),
                phone = Phone("+965", phoneNumber.get().toString())
            )

            if (prefs.existKey(USER_ID)) {
                callback = LocationCallbackRequest(
                    fullName = fullNameField.get().toString(),
                    phone = Phone("+965", phoneNumber.get().toString())

                    )
                callback.client = prefs.string(USER_ID)

            }

            callback.location = location.id
            if (isCivilIdMandatory.get()) {
                callback.civilId = civilId.get()
            }
            Log.e(TAG, "requestCallback: " + callback.toString())
            callbackLoading.value =true
            disposable.add(
                service.sendCallbackLocation(callback)
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
                            callbackLoading.value =false
                        }

                        override fun onError(e: Throwable) {
                            e.let { it.message?.let { it1 -> Log.e("RESPONSE_ERROR", it1) } }
                            callbackLoading.value =false
                            callbackError.value = true
                            verifyPhone.value = false
//                            nameError.set(true)
//                            phoneError.set(true)
                        }

                    })
            )
        }
    }

    private fun saveCallback(t: Callback) {
        local.saveObject(t) {
            t.client?.id?.let { prefs.setString(USER_ID, it) }
            Log.e(TAG, "saveCallback: $it")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}